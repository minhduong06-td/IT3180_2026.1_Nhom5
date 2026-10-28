package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Apartment;
import com.bluemoon.model.Domain.Contribution;
import com.bluemoon.model.Domain.ContributionRecord;
import com.bluemoon.model.Domain.FeeType;
import com.bluemoon.model.Domain.PaymentMethod;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

final class ContributionView extends VBox {
    private final AppServices services;
    private final AppUser user;
    private final TableView<ContributionRecord> table = new TableView<>();
    private final Label total = Ui.label("Tổng đã nhận: 0 ₫", "section-title");

    ContributionView(AppServices services, AppUser user) {
        this.services = services;
        this.user = user;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        Button record = Ui.button("Ghi nhận đóng góp", true);
        record.setOnAction(event -> record());
        Button refresh = Ui.button("Làm mới", false);
        refresh.setOnAction(event -> load());
        table.setPlaceholder(new Label("Chưa có khoản đóng góp được ghi nhận."));
        getChildren().addAll(Ui.pageHeader("Đóng góp", "Theo dõi số tiền thực nhận theo từng căn hộ và khoản đóng góp."),
                Ui.toolbar(total, Ui.spacer(), refresh, record), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        load();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Ngày nhận", row -> row.receivedOn().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        table.getColumns().add(column("Căn hộ", ContributionRecord::apartmentCode));
        table.getColumns().add(column("Chủ hộ", ContributionRecord::householdName));
        table.getColumns().add(column("Khoản đóng góp", ContributionRecord::feeName));
        table.getColumns().add(column("Số tiền", row -> Ui.money(row.amount())));
        table.getColumns().add(column("Hình thức", row -> method(row.method())));
        table.getColumns().add(column("Mã giao dịch", row -> empty(row.reference())));
        table.getColumns().add(column("Ghi chú", row -> empty(row.note())));
    }

    private void load() {
        try {
            List<ContributionRecord> rows = services.contributions().list(user);
            table.getItems().setAll(rows);
            BigDecimal received = rows.stream().map(ContributionRecord::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
            total.setText("Tổng đã nhận: " + Ui.money(received));
        } catch (SQLException exception) {
            Ui.error("Không tải được khoản đóng góp", exception);
        }
    }

    private void record() {
        List<FeeType> fees;
        List<Apartment> apartments;
        try {
            fees = services.fees().list(user).stream().filter(fee -> !fee.required() && fee.active()).toList();
            apartments = services.apartments().list(user).stream().filter(Apartment::active).toList();
        } catch (SQLException exception) {
            Ui.error("Không tải được danh mục", exception);
            return;
        }
        if (fees.isEmpty()) {
            Ui.info("Chưa có khoản đóng góp", "Ban quản trị cần tạo khoản đóng góp trước khi ghi nhận tiền.");
            return;
        }
        if (apartments.isEmpty()) {
            Ui.info("Chưa có căn hộ", "Cần có căn hộ đang hoạt động để ghi nhận đóng góp.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Ghi nhận đóng góp");
        dialog.setHeaderText("Nhập thông tin số tiền thực nhận");
        dialog.initOwner(getScene().getWindow());
        ButtonType save = new ButtonType("Lưu khoản thu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);

        ComboBox<FeeType> fee = new ComboBox<>();
        fee.getItems().setAll(fees);
        fee.setValue(fees.getFirst());
        fee.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(FeeType value) { return value == null ? "" : value.name(); }
            @Override public FeeType fromString(String value) { return null; }
        });
        style(fee);
        ComboBox<Apartment> apartment = new ComboBox<>();
        apartment.getItems().setAll(apartments);
        apartment.setValue(apartments.getFirst());
        apartment.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Apartment value) {
                return value == null ? "" : value.code() + " · " + value.householdName();
            }
            @Override public Apartment fromString(String value) { return null; }
        });
        style(apartment);
        var amount = Ui.textField("Số tiền VND");
        var receivedOn = new DatePicker(LocalDate.now());
        style(receivedOn);
        ComboBox<PaymentMethod> method = new ComboBox<>();
        method.getItems().setAll(PaymentMethod.values());
        method.setValue(PaymentMethod.CASH);
        method.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(PaymentMethod value) { return value == null ? "" : ContributionView.method(value); }
            @Override public PaymentMethod fromString(String value) { return PaymentMethod.CASH; }
        });
        style(method);
        var reference = Ui.textField("Mã giao dịch (không bắt buộc)");
        var note = Ui.textField("Ghi chú (không bắt buộc)");
        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        addRow(form, 0, "Khoản đóng góp", fee);
        addRow(form, 1, "Căn hộ", apartment);
        addRow(form, 2, "Số tiền (VND)", amount);
        addRow(form, 3, "Ngày nhận", receivedOn);
        addRow(form, 4, "Hình thức", method);
        addRow(form, 5, "Mã giao dịch", reference);
        addRow(form, 6, "Ghi chú", note);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(560);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                services.contributions().record(user, new Contribution(fee.getValue().id(), apartment.getValue().id(),
                        new BigDecimal(amount.getText().trim().replace(',', '.')), receivedOn.getValue(), method.getValue(),
                        reference.getText(), note.getText()));
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không ghi nhận được đóng góp", exception);
            }
        });
        if (dialog.showAndWait().filter(save::equals).isPresent()) load();
    }

    private static void addRow(GridPane grid, int row, String title, javafx.scene.Node field) {
        Label label = new Label(title);
        label.getStyleClass().add("form-label");
        label.setLabelFor(field);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private static void style(javafx.scene.control.Control control) {
        control.getStyleClass().add("form-field");
    }

    private static String method(PaymentMethod method) {
        return method == PaymentMethod.CASH ? "Tiền mặt" : "Chuyển khoản";
    }

    private static String empty(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private static TableColumn<ContributionRecord, String> column(String title,
                                                                    Function<ContributionRecord, String> value) {
        TableColumn<ContributionRecord, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
