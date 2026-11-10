package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.DashboardSummary;
import com.bluemoon.model.Domain.PaymentRecord;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

final class DashboardView extends VBox {
    DashboardView(AppServices services, AppUser user, Runnable createPeriod) {
        setSpacing(20);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        try {
            DashboardSummary summary = services.dashboard().summary(user);
            List<PaymentRecord> payments = services.collections().payments(user, "").stream().limit(6).toList();
            HBox heading = new HBox(12, new VBox(4,
                    Ui.label("Tổng quan", "page-title"),
                    Ui.label("Chưa phát hành".equals(summary.period()) ? summary.period() : "Kỳ " + summary.period(),
                            "page-subtitle")), Ui.spacer());
            heading.setAlignment(Pos.CENTER_LEFT);
            if (user.role() == com.bluemoon.model.Domain.Role.ADMIN && "Chưa phát hành".equals(summary.period())) {
                var create = Ui.button("Tạo kỳ thu", true);
                create.setOnAction(event -> createPeriod.run());
                heading.getChildren().add(create);
            }
            HBox summaries = new HBox(12,
                    metric("Tổng phải thu", Ui.money(summary.totalAmount()), ""),
                    metric("Đã thanh toán", Ui.money(summary.paidAmount()), "summary-accent"),
                    metric("Còn phải thu", Ui.money(summary.balance()), "summary-warn"));
            for (Node node : summaries.getChildren()) HBox.setHgrow(node, Priority.ALWAYS);

            VBox collection = new VBox(12, Ui.label("Các khoản thu gần đây", "section-title"));
            collection.getStyleClass().add("surface");
            if (payments.isEmpty()) {
                VBox empty = new VBox(8, Ui.label("Chưa có giao dịch.", "muted"));
                empty.getStyleClass().add("empty-state");
                collection.getChildren().add(empty);
            } else {
                collection.getChildren().add(paymentTable(payments));
            }
            var note = Ui.label(summary.apartmentCount() + " căn hộ  ·  "
                    + summary.paidCharges() + " khoản hoàn tất  ·  "
                    + summary.openCharges() + " khoản còn nợ", "muted");
            getChildren().addAll(heading, summaries, note, collection);
        } catch (SQLException exception) {
            Ui.error("Không tải được tổng quan", exception);
            getChildren().add(Ui.label("Không tải được số liệu. Vui lòng thử lại.", "inline-error"));
        }
    }

    private static VBox metric(String label, String value, String valueStyle) {
        var title = Ui.label(label, "summary-label");
        var amount = Ui.label(value, "summary-value");
        if (!valueStyle.isBlank()) amount.getStyleClass().add(valueStyle);
        VBox box = new VBox(10, title, amount);
        box.getStyleClass().add("summary-item");
        return box;
    }

    private static TableView<PaymentRecord> paymentTable(List<PaymentRecord> rows) {
        TableView<PaymentRecord> table = new TableView<>();
        table.setPrefHeight(300);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Ngày thu", row -> row.paidAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        table.getColumns().add(column("Căn hộ", PaymentRecord::apartmentCode));
        table.getColumns().add(column("Người nộp", PaymentRecord::payerName));
        table.getColumns().add(column("Khoản thu", PaymentRecord::feeName));
        table.getColumns().add(column("Kỳ", PaymentRecord::period));
        table.getColumns().add(column("Số tiền", row -> Ui.money(row.amount())));
        table.getItems().setAll(rows);
        return table;
    }

    private static TableColumn<PaymentRecord, String> column(String title, Function<PaymentRecord, String> value) {
        TableColumn<PaymentRecord, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
