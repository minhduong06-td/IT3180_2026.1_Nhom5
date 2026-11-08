package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.BillingPeriod;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.LongConsumer;

final class BillingView extends VBox {
    private final AppServices services;
    private final AppUser user;
    private final LongConsumer viewCharges;
    private final TableView<BillingPeriod> table = new TableView<>();

    BillingView(AppServices services, AppUser user, LongConsumer viewCharges) {
        this.services = services;
        this.user = user;
        this.viewCharges = viewCharges;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        var create = Ui.button("Phát hành kỳ thu", true);
        create.setOnAction(event -> issue());
        var details = Ui.button("Xem bảng kê", false);
        details.setOnAction(event -> {
            BillingPeriod selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) Ui.info("Chọn kỳ thu", "Chọn một kỳ để mở bảng kê.");
            else viewCharges.accept(selected.id());
        });
        var refresh = Ui.button("Làm mới", false);
        refresh.setOnAction(event -> load());
        getChildren().addAll(Ui.pageHeader("Kỳ thu", "Phát hành mỗi tháng một lần. Các khoản đã tạo không thể phát hành trùng."),
                Ui.toolbar(Ui.label("Phí và diện tích được chụp lại tại thời điểm phát hành.", "muted"),
                        Ui.spacer(), refresh, details, create), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        load();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Kỳ thu", BillingPeriod::label));
        table.getColumns().add(column("Ngày phát hành", period -> Ui.date(period.issueDate())));
        table.getColumns().add(column("Hạn nộp", period -> Ui.date(period.dueDate())));
        table.getColumns().add(column("Số khoản", period -> Integer.toString(period.chargeCount())));
        table.getColumns().add(column("Tổng phải thu", period -> Ui.money(period.totalAmount())));
        table.setRowFactory(view -> {
            TableRow<BillingPeriod> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) viewCharges.accept(row.getItem().id());
            });
            return row;
        });
    }

    private void load() {
        try {
            table.getItems().setAll(services.billing().list(user));
        } catch (SQLException exception) {
            Ui.error("Không tải được kỳ thu", exception);
        }
    }

    private void issue() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Phát hành kỳ thu");
        dialog.setHeaderText("Tạo bảng kê cho các căn hộ đang hoạt động");
        dialog.initOwner(getScene().getWindow());
        ButtonType publish = new ButtonType("Phát hành", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(publish, ButtonType.CANCEL);

        var month = new Spinner<Integer>(1, 12, 10);
        var year = new Spinner<Integer>(2000, 2200, 2026);
        month.setEditable(true);
        year.setEditable(true);
        var issueDate = new DatePicker(LocalDate.of(2026, 10, 1));
        var dueDate = new DatePicker(LocalDate.of(2026, 10, 31));
        month.valueProperty().addListener((observable, oldValue, newValue) -> datesFor(newValue, year.getValue(), issueDate, dueDate));
        year.valueProperty().addListener((observable, oldValue, newValue) -> datesFor(month.getValue(), newValue, issueDate, dueDate));
        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        addRow(form, 0, "Tháng", month);
        addRow(form, 1, "Năm", year);
        addRow(form, 2, "Ngày phát hành", issueDate);
        addRow(form, 3, "Hạn nộp", dueDate);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(480);
        AtomicReference<BillingPeriod> result = new AtomicReference<>();
        Button publishButton = (Button) dialog.getDialogPane().lookupButton(publish);
        publishButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận phát hành");
            confirm.setHeaderText("Phát hành kỳ %02d/%d?".formatted(month.getValue(), year.getValue()));
            confirm.setContentText("Hệ thống sẽ tạo một khoản cho mỗi căn hộ và phí bắt buộc. Kỳ thu này không thể tạo trùng.");
            confirm.initOwner(getScene().getWindow());
            if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                event.consume();
                return;
            }
            try {
                result.set(services.billing().issue(user, year.getValue(), month.getValue(),
                        issueDate.getValue(), dueDate.getValue()));
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không phát hành được kỳ thu", exception);
            }
        });
        dialog.showAndWait();
        load();
        BillingPeriod issued = result.get();
        if (issued != null) Ui.info("Đã phát hành kỳ thu", "Đã tạo " + issued.chargeCount()
                + " khoản phải thu, tổng cộng " + Ui.money(issued.totalAmount()) + ".");
    }

    private static void datesFor(int month, int year, DatePicker issue, DatePicker due) {
        LocalDate first = LocalDate.of(year, month, 1);
        issue.setValue(first);
        due.setValue(first.withDayOfMonth(first.lengthOfMonth()));
    }

    private static void addRow(GridPane grid, int row, String title, javafx.scene.Node field) {
        Label label = new Label(title);
        label.getStyleClass().add("form-label");
        label.setLabelFor(field);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private static TableColumn<BillingPeriod, String> column(String title, Function<BillingPeriod, String> value) {
        TableColumn<BillingPeriod, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
