package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.BillingPeriod;
import com.bluemoon.model.Domain.CalculationBasis;
import com.bluemoon.model.Domain.ChargeStatus;
import com.bluemoon.model.Domain.ChargeSummary;
import com.bluemoon.model.Domain.PaymentMethod;
import com.bluemoon.model.Domain.Role;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

final class ChargesView extends VBox {
    private final AppServices services;
    private final AppUser user;
    private final Long initialPeriodId;
    private final ComboBox<String> periodFilter = new ComboBox<>();
    private final Map<String, Long> periodIds = new HashMap<>();
    private final javafx.scene.control.TextField search = Ui.textField("Tìm mã căn, chủ hộ, khoản thu");
    private final TableView<ChargeSummary> table = new TableView<>();

    ChargesView(AppServices services, AppUser user) {
        this(services, user, null);
    }

    ChargesView(AppServices services, AppUser user, Long initialPeriodId) {
        this.services = services;
        this.user = user;
        this.initialPeriodId = initialPeriodId;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        periodFilter.setPromptText("Tất cả kỳ thu");
        periodFilter.getStyleClass().add("form-field");
        var refresh = Ui.button("Tìm kiếm", true);
        refresh.setOnAction(event -> load());
        search.setOnAction(event -> load());
        var statement = Ui.button("Xem bảng kê căn hộ", false);
        statement.setOnAction(event -> showStatement());
        var pay = Ui.button("Ghi nhận thanh toán", true);
        pay.setVisible(user.role() == Role.ACCOUNTANT);
        pay.setManaged(user.role() == Role.ACCOUNTANT);
        pay.setOnAction(event -> recordPayment());
        table.setPlaceholder(new Label("Không có khoản phải thu phù hợp."));
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldRow, row) -> {
            statement.setDisable(row == null);
            pay.setDisable(row == null || row.status() == ChargeStatus.PAID);
        });
        getChildren().addAll(Ui.pageHeader("Bảng kê phải thu", "Tra cứu từng khoản, công thức tính và số dư của căn hộ."),
                Ui.toolbar(periodFilter, search, refresh, Ui.spacer(), statement, pay), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        loadPeriods();
        load();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Căn hộ", ChargeSummary::apartmentCode));
        table.getColumns().add(column("Khoản thu", ChargeSummary::feeName));
        table.getColumns().add(column("Kỳ", ChargeSummary::period));
        table.getColumns().add(column("Cách tính", ChargesView::formula));
        table.getColumns().add(column("Phải thu", charge -> Ui.money(charge.amount())));
        table.getColumns().add(column("Đã thu", charge -> Ui.money(charge.paid())));
        table.getColumns().add(column("Còn nợ", charge -> Ui.money(charge.balance())));
        TableColumn<ChargeSummary, String> status = column("Trạng thái", charge -> status(charge.status()));
        status.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : value);
                getStyleClass().removeAll("status-paid", "status-partial", "status-unpaid");
                if (empty) return;
                ChargeSummary item = getTableRow() == null ? null : getTableRow().getItem();
                if (item != null) getStyleClass().add(switch (item.status()) {
                    case PAID -> "status-paid";
                    case PARTIALLY_PAID -> "status-partial";
                    case UNPAID -> "status-unpaid";
                });
            }
        });
        table.getColumns().add(status);
        table.getColumns().add(column("Hạn nộp", charge -> Ui.date(charge.dueDate())));
        table.setRowFactory(view -> {
            TableRow<ChargeSummary> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) showStatement(row.getItem());
            });
            return row;
        });
    }

    private void loadPeriods() {
        try {
            List<BillingPeriod> periods = services.billing().list(user);
            periodIds.clear();
            periodFilter.getItems().clear();
            periodFilter.getItems().add("Tất cả các kỳ");
            for (BillingPeriod period : periods) {
                periodFilter.getItems().add(period.label());
                periodIds.put(period.label(), period.id());
            }
            periodFilter.setValue(initialPeriodId == null ? "Tất cả các kỳ"
                    : periods.stream().filter(period -> period.id() == initialPeriodId).map(BillingPeriod::label)
                    .findFirst().orElse("Tất cả các kỳ"));
        } catch (SQLException exception) {
            Ui.error("Không tải được kỳ thu", exception);
        }
    }

    private void load() {
        try {
            Long periodId = periodIds.get(periodFilter.getValue());
            table.getItems().setAll(services.collections().charges(user, periodId, search.getText()));
        } catch (SQLException exception) {
            Ui.error("Không tải được bảng kê", exception);
        }
    }

    private void showStatement() {
        ChargeSummary selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) showStatement(selected);
    }

    private void showStatement(ChargeSummary selected) {
        try {
            List<ChargeSummary> items = services.collections().charges(user, selected.periodId(), selected.apartmentCode())
                    .stream().filter(charge -> charge.apartmentCode().equals(selected.apartmentCode())).toList();
            TableView<ChargeSummary> statement = new TableView<>();
            statement.setPrefHeight(240);
            statement.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            statement.getColumns().add(column("Khoản thu", ChargeSummary::feeName));
            statement.getColumns().add(column("Công thức", ChargesView::formula));
            statement.getColumns().add(column("Phải thu", charge -> Ui.money(charge.amount())));
            statement.getColumns().add(column("Đã thu", charge -> Ui.money(charge.paid())));
            statement.getColumns().add(column("Còn nợ", charge -> Ui.money(charge.balance())));
            statement.getItems().setAll(items);
            BigDecimal total = items.stream().map(ChargeSummary::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal balance = items.stream().map(ChargeSummary::balance).reduce(BigDecimal.ZERO, BigDecimal::add);
            Label title = Ui.label(selected.apartmentCode() + "  ·  " + selected.householdName(), "section-title");
            Label due = Ui.label("Kỳ " + selected.period() + "  ·  Hạn nộp " + Ui.date(selected.dueDate()), "muted");
            Label totals = Ui.label("Tổng phải thu " + Ui.money(total) + "  ·  Còn nợ " + Ui.money(balance), "section-title");
            VBox content = new VBox(12, title, due, statement, totals);
            content.setPadding(new Insets(8, 0, 2, 0));
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Bảng kê căn hộ");
            alert.setHeaderText("Bảng kê khoản thu");
            alert.getDialogPane().setContent(content);
            alert.getDialogPane().setMinWidth(820);
            alert.initOwner(getScene().getWindow());
            alert.showAndWait();
        } catch (SQLException exception) {
            Ui.error("Không tải được bảng kê căn hộ", exception);
        }
    }

    private void recordPayment() {
        ChargeSummary charge = table.getSelectionModel().getSelectedItem();
        if (charge == null) return;
        if (charge.status() == ChargeStatus.PAID) {
            Ui.info("Đã thanh toán đủ", "Khoản này không còn công nợ.");
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ghi nhận thanh toán");
        dialog.setHeaderText(charge.apartmentCode() + "  ·  " + charge.feeName() + "  ·  " + charge.period());
        dialog.initOwner(getScene().getWindow());
        ButtonType save = new ButtonType("Lưu giao dịch", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);
        var amount = Ui.textField(charge.balance().toPlainString());
        amount.setText(charge.balance().toPlainString());
        var method = new ComboBox<PaymentMethod>();
        method.getItems().setAll(PaymentMethod.values());
        method.setValue(PaymentMethod.CASH);
        method.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(PaymentMethod value) {
                return value == PaymentMethod.CASH ? "Tiền mặt" : "Chuyển khoản thủ công";
            }
            @Override public PaymentMethod fromString(String value) { return PaymentMethod.CASH; }
        });
        method.getStyleClass().add("form-field");
        var reference = Ui.textField("Mã giao dịch (không bắt buộc)");
        var note = Ui.textField("Ghi chú (không bắt buộc)");
        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        addRow(form, 0, "Còn nợ", new Label(Ui.money(charge.balance())));
        addRow(form, 1, "Số tiền thu (VND)", amount);
        addRow(form, 2, "Hình thức", method);
        addRow(form, 3, "Mã giao dịch", reference);
        addRow(form, 4, "Ghi chú", note);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(540);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                BigDecimal paid = new BigDecimal(amount.getText().trim().replace(',', '.'));
                services.collections().recordPayment(user, charge.id(), paid, method.getValue(),
                        reference.getText(), note.getText());
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không ghi nhận được thanh toán", exception);
            }
        });
        dialog.showAndWait();
        load();
    }

    private static String formula(ChargeSummary charge) {
        if (charge.basis() == CalculationBasis.PER_M2) {
            return charge.quantity().stripTrailingZeros().toPlainString() + " m² × " + Ui.money(charge.unitPrice());
        }
        return "Mức cố định " + Ui.money(charge.unitPrice());
    }

    private static String status(ChargeStatus status) {
        return switch (status) {
            case UNPAID -> "Chưa thu";
            case PARTIALLY_PAID -> "Thu một phần";
            case PAID -> "Đã thanh toán";
        };
    }

    private static void addRow(GridPane grid, int row, String title, javafx.scene.Node field) {
        Label label = new Label(title);
        label.getStyleClass().add("form-label");
        label.setLabelFor(field);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private static TableColumn<ChargeSummary, String> column(String title, Function<ChargeSummary, String> value) {
        TableColumn<ChargeSummary, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }

}
