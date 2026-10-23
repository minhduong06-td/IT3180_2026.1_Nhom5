package com.bluemoon.ui;

import com.bluemoon.model.Domain.CalculationBasis;
import com.bluemoon.model.Domain.ChargeSummary;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.util.List;

final class FeeNoticePrinter {
    private FeeNoticePrinter() {}

    static void print(Window owner, List<List<ChargeSummary>> households) {
        if (households.isEmpty()) {
            Ui.info("Không có thông báo", "Kỳ thu này chưa có bảng kê để in.");
            return;
        }
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            Ui.info("Không tìm thấy máy in", "Kết nối máy in hoặc chọn máy in PDF trong cài đặt hệ thống.");
            return;
        }
        job.getJobSettings().setJobName("Thông báo thu phí BlueMoon");
        if (!job.showPrintDialog(owner)) {
            job.cancelJob();
            return;
        }
        try {
            List<VBox> pages = new java.util.ArrayList<>(households.size());
            for (List<ChargeSummary> charges : households) {
                VBox page = page(charges);
                Scene scene = new Scene(page, 700, 1200);
                scene.getStylesheets().add(FeeNoticePrinter.class.getResource("/ui/app.css").toExternalForm());
                page.applyCss();
                double pageHeight = page.prefHeight(700);
                page.resize(700, pageHeight);
                page.layout();
                double printableWidth = job.getJobSettings().getPageLayout().getPrintableWidth() * 4 / 3;
                double printableHeight = job.getJobSettings().getPageLayout().getPrintableHeight() * 4 / 3;
                double scale = Math.min(1, Math.min(printableWidth / page.getLayoutBounds().getWidth(),
                        printableHeight / page.getLayoutBounds().getHeight()));
                page.getTransforms().setAll(new Scale(scale, scale, 0, 0));
                pages.add(page);
            }
            Thread printer = new Thread(() -> send(job, pages), "bluemoon-fee-notice-print");
            printer.setDaemon(true);
            printer.start();
        } catch (RuntimeException exception) {
            job.cancelJob();
            Ui.error("Không in được thông báo", exception);
        }
    }

    private static void send(PrinterJob job, List<VBox> pages) {
        try {
            for (VBox page : pages) {
                if (!job.printPage(page)) throw new IllegalStateException("Không gửi được một trang đến máy in.");
            }
            if (!job.endJob()) throw new IllegalStateException("Không hoàn tất được lệnh in.");
        } catch (RuntimeException exception) {
            job.cancelJob();
            Platform.runLater(() -> Ui.error("Không in được thông báo", exception));
        }
    }

    private static VBox page(List<ChargeSummary> charges) {
        ChargeSummary first = charges.getFirst();
        BigDecimal total = charges.stream().map(ChargeSummary::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = charges.stream().map(ChargeSummary::paid).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = total.subtract(paid);
        String area = charges.stream().filter(charge -> charge.basis() == CalculationBasis.PER_M2)
                .map(charge -> charge.quantity().stripTrailingZeros().toPlainString()).findFirst().orElse("—");
        String residents = charges.stream().filter(charge -> charge.basis() == CalculationBasis.PER_RESIDENT)
                .map(charge -> charge.quantity().stripTrailingZeros().toPlainString()).findFirst().orElse("—");
        VBox page = new VBox(14);
        page.getStyleClass().add("fee-notice-page");
        page.setPadding(new Insets(36));
        page.setMinWidth(700);
        page.setPrefWidth(700);
        page.setMaxWidth(700);
        Label organization = label("BAN QUẢN TRỊ TÒA NHÀ CHUNG CƯ BLUEMOON", "notice-organization");
        organization.setAlignment(Pos.CENTER);
        organization.setMaxWidth(Double.MAX_VALUE);
        Label title = label("GIẤY THÔNG BÁO THU PHÍ THÁNG " + first.period(), "notice-title");
        title.setAlignment(Pos.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        Label household = label("Căn hộ " + first.apartmentCode() + "    Chủ hộ: " + first.householdName(), "notice-heading");
        Label details = label("Diện tích tính phí: " + area + " m²    Nhân khẩu: " + residents + " người", "notice-body");

        GridPane rows = new GridPane();
        rows.setHgap(10);
        rows.setVgap(8);
        rows.add(label("Khoản thu", "notice-table-head"), 0, 0);
        rows.add(label("Cách tính", "notice-table-head"), 1, 0);
        rows.add(label("Số tiền", "notice-table-head"), 2, 0);
        rows.getColumnConstraints().setAll(column(210), column(240), column(130));
        for (int index = 0; index < charges.size(); index++) {
            ChargeSummary charge = charges.get(index);
            int row = index + 1;
            rows.add(label(charge.feeName(), "notice-body"), 0, row);
            rows.add(label(ChargesView.formula(charge), "notice-body"), 1, row);
            Label amount = label(Ui.money(charge.amount()), "notice-body");
            amount.setAlignment(Pos.CENTER_RIGHT);
            rows.add(amount, 2, row);
        }

        Label bank = label(bankInformation(), "notice-body");
        Label transferText = label("Nội dung chuyển khoản: " + transferDescription(first), "notice-body");
        Label due = label("Hạn nộp: " + Ui.date(first.dueDate()), "notice-heading");
        VBox totals = new VBox(6,
                label("Tổng phải nộp: " + Ui.money(total), "notice-total"),
                label("Đã thanh toán: " + Ui.money(paid), "notice-body"),
                label("Còn phải nộp: " + Ui.money(balance), "notice-total"));
        VBox signature = new VBox(6);
        signature.setAlignment(Pos.CENTER_RIGHT);
        Label signSpace = label(" ", "notice-body");
        signSpace.setMinHeight(54);
        signature.getChildren().addAll(label("ĐẠI DIỆN BAN QUẢN TRỊ", "notice-heading"), signSpace,
                label("Ban quản trị BlueMoon", "notice-body"));
        page.getChildren().addAll(organization, title, new Separator(), household, details,
                rows, new Separator(), totals, due, bank, transferText,
                label("Có thể nộp tại Văn phòng Ban quản trị (Tầng 1).", "notice-body"), signature);
        return page;
    }

    private static ColumnConstraints column(double width) {
        ColumnConstraints column = new ColumnConstraints(width);
        column.setHgrow(Priority.ALWAYS);
        return column;
    }

    private static Label label(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        label.setWrapText(true);
        return label;
    }

    static String bankInformation() {
        String bankName = setting("BLUEMOON_BANK_NAME");
        String bankAccount = setting("BLUEMOON_BANK_ACCOUNT");
        String bankHolder = setting("BLUEMOON_BANK_HOLDER");
        return bankName.isEmpty() || bankAccount.isEmpty() || bankHolder.isEmpty()
                ? "Thông tin chuyển khoản: Liên hệ Ban quản trị."
                : "Chuyển khoản: " + bankName + " · " + bankAccount + " · " + bankHolder;
    }

    static String transferDescription(ChargeSummary charge) {
        return "%s Dong phi thang %s".formatted(charge.apartmentCode(), charge.period());
    }

    private static String setting(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value.trim();
    }
}
