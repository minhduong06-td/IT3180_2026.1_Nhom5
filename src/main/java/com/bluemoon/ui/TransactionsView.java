package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.PaymentMethod;
import com.bluemoon.model.Domain.PaymentRecord;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

final class TransactionsView extends VBox {
    private final AppServices services;
    private final AppUser user;
    private final javafx.scene.control.TextField search = Ui.textField("Tìm căn hộ, người nộp, khoản thu, mã giao dịch");
    private final TableView<PaymentRecord> table = new TableView<>();
    private final javafx.scene.control.Button exportCsv = Ui.button("Xuất CSV", false);

    TransactionsView(AppServices services, AppUser user) {
        this.services = services;
        this.user = user;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        var find = Ui.button("Tìm kiếm", true);
        find.setOnAction(event -> load());
        search.setOnAction(event -> load());
        exportCsv.setDisable(true);
        exportCsv.setOnAction(event -> exportVisibleRows());
        table.setPlaceholder(new Label("Chưa có giao dịch phù hợp."));
        getChildren().addAll(Ui.pageHeader("Lịch sử thu", "Tra cứu các giao dịch đã ghi nhận để đối soát."),
                Ui.toolbar(search, find, Ui.spacer(), exportCsv), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        load();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Ngày thu", payment -> payment.paidAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        table.getColumns().add(column("Căn hộ", PaymentRecord::apartmentCode));
        table.getColumns().add(column("Chủ hộ", PaymentRecord::householdName));
        table.getColumns().add(column("Người nộp", PaymentRecord::payerName));
        table.getColumns().add(column("Khoản thu", PaymentRecord::feeName));
        table.getColumns().add(column("Kỳ", PaymentRecord::period));
        table.getColumns().add(column("Số tiền nhận", payment -> Ui.money(payment.amount())));
        table.getColumns().add(column("Đã cấn trừ", payment -> Ui.money(payment.appliedAmount())));
        table.getColumns().add(column("Nộp dư", payment -> Ui.money(payment.amount().subtract(payment.appliedAmount()))));
        table.getColumns().add(column("Hình thức", payment -> payment.method() == PaymentMethod.CASH ? "Tiền mặt" : "Chuyển khoản"));
        table.getColumns().add(column("Mã giao dịch", payment -> empty(payment.reference())));
        table.getColumns().add(column("Ghi chú", payment -> empty(payment.note())));
        table.getColumns().add(column("Người ghi nhận", PaymentRecord::recordedBy));
    }

    private void load() {
        try {
            List<PaymentRecord> rows = services.collections().payments(user, search.getText());
            table.getItems().setAll(rows);
            exportCsv.setDisable(rows.isEmpty());
        } catch (SQLException exception) {
            Ui.error("Không tải được lịch sử thu", exception);
        }
    }

    private void exportVisibleRows() {
        if (table.getItems().isEmpty()) return;
        DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        List<List<String>> rows = table.getItems().stream().map(payment -> List.of(
                payment.paidAt().format(dateTime), payment.apartmentCode(), payment.householdName(),
                payment.payerName(), payment.feeName(), payment.period(), payment.amount().toPlainString(),
                payment.appliedAmount().toPlainString(), payment.amount().subtract(payment.appliedAmount()).toPlainString(),
                payment.method() == PaymentMethod.CASH ? "Tiền mặt" : "Chuyển khoản", empty(payment.reference()),
                empty(payment.note()), payment.recordedBy())).toList();
        CsvExporter.save(getScene().getWindow(), "Xuất lịch sử thu", "lich-su-thu.csv",
                List.of("Ngày thu", "Căn hộ", "Chủ hộ", "Người nộp", "Khoản thu", "Kỳ", "Số tiền nhận (VND)",
                        "Đã cấn trừ (VND)", "Nộp dư (VND)", "Hình thức", "Mã giao dịch", "Ghi chú", "Người ghi nhận"), rows);
    }

    private static String empty(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static TableColumn<PaymentRecord, String> column(String title, Function<PaymentRecord, String> value) {
        TableColumn<PaymentRecord, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
