package com.bluemoon.ui;

import com.bluemoon.model.Domain.Apartment;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.UtilityBill;
import com.bluemoon.model.Domain.UtilityBillInput;
import com.bluemoon.model.Domain.UtilityType;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

final class UtilityView extends VBox {
    private final AppServices services;
    private final AppUser user;
    private final TextField search = Ui.textField("Tìm căn hộ, nhà cung cấp hoặc mã đối chiếu");
    private final TableView<UtilityBill> table = new TableView<>();
    private final Button exportCsv = Ui.button("Xuất CSV", false);

    UtilityView(AppServices services, AppUser user) {
        this.services = services;
        this.user = user;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        Button add = Ui.button("Nhập hóa đơn", true);
        add.setOnAction(event -> edit(null));
        Button refresh = Ui.button("Làm mới", false);
        refresh.setOnAction(event -> load());
        exportCsv.setDisable(true);
        exportCsv.setOnAction(event -> exportVisibleRows());
        search.setOnAction(event -> load());
        getChildren().addAll(Ui.pageHeader("Điện nước internet", ""),
                Ui.toolbar(search, refresh, exportCsv, add), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        load();
    }

    private void configureTable() {
        table.setPlaceholder(new Label("Chưa có hóa đơn tiện ích."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Kỳ", bill -> "%02d/%d".formatted(bill.month(), bill.year())));
        table.getColumns().add(column("Căn hộ", UtilityBill::apartmentCode));
        table.getColumns().add(column("Dịch vụ", bill -> typeName(bill.type())));
        table.getColumns().add(column("Nhà cung cấp", UtilityBill::provider));
        table.getColumns().add(column("Số tiền", bill -> Ui.money(bill.amount())));
        table.getColumns().add(column("Mã đối chiếu", bill -> bill.reference() == null ? "—" : bill.reference()));
        table.getColumns().add(column("Trạng thái", bill -> bill.issued() ? "Đã phát hành" : "Chưa phát hành"));
        table.getColumns().add(Ui.actionColumn("Thao tác", bill -> {
            Button change = Ui.button("Sửa", false);
            Button remove = Ui.button("Xóa", false);
            boolean locked = bill.issued();
            change.setDisable(locked);
            remove.setDisable(locked);
            change.setOnAction(event -> edit(bill));
            remove.setOnAction(event -> delete(bill));
            return new HBox(8, change, remove);
        }));
        table.setSelectionModel(null);
    }

    private void load() {
        try {
            String term = search.getText() == null ? "" : search.getText().trim().toLowerCase(Locale.ROOT);
            List<UtilityBill> bills = services.utilities().list(user).stream()
                    .filter(bill -> term.isEmpty() || bill.apartmentCode().toLowerCase(Locale.ROOT).contains(term)
                            || bill.provider().toLowerCase(Locale.ROOT).contains(term)
                            || bill.reference() != null && bill.reference().toLowerCase(Locale.ROOT).contains(term))
                    .toList();
            table.getItems().setAll(bills);
            exportCsv.setDisable(bills.isEmpty());
        } catch (SQLException exception) {
            Ui.error("Không tải được hóa đơn tiện ích", exception);
        }
    }

    private void exportVisibleRows() {
        if (table.getItems().isEmpty()) return;
        List<List<String>> rows = table.getItems().stream().map(bill -> List.of(
                "%02d/%d".formatted(bill.month(), bill.year()), bill.apartmentCode(), typeName(bill.type()),
                bill.provider(), bill.amount().toPlainString(), bill.reference() == null ? "—" : bill.reference(),
                bill.issued() ? "Đã phát hành" : "Chưa phát hành")).toList();
        CsvExporter.save(getScene().getWindow(), "Xuất hóa đơn tiện ích", "hoa-don-tien-ich.csv",
                List.of("Kỳ", "Căn hộ", "Dịch vụ", "Nhà cung cấp", "Số tiền (VND)", "Mã đối chiếu", "Trạng thái"), rows);
    }

    private void edit(UtilityBill current) {
        List<Apartment> apartments;
        try {
            apartments = services.apartments().list(user).stream()
                    .filter(apartment -> apartment.active()
                            || current != null && apartment.id().equals(current.apartmentId()))
                    .toList();
        } catch (SQLException exception) {
            Ui.error("Không tải được danh sách căn hộ", exception);
            return;
        }
        if (apartments.isEmpty()) {
            Ui.info("Chưa có căn hộ hoạt động", "Thêm hoặc kích hoạt căn hộ trước khi nhập hóa đơn.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(current == null ? "Nhập hóa đơn tiện ích" : "Cập nhật hóa đơn tiện ích");
        dialog.setHeaderText("Thông tin hóa đơn của căn hộ");
        dialog.initOwner(getScene().getWindow());
        ButtonType save = new ButtonType("Lưu hóa đơn", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);

        ComboBox<Apartment> apartment = new ComboBox<>();
        apartment.getItems().setAll(apartments);
        apartment.setValue(current == null ? apartments.getFirst()
                : apartments.stream().filter(item -> item.id().equals(current.apartmentId())).findFirst().orElse(null));
        apartment.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Apartment value) {
                return value == null ? "" : value.code() + " · " + value.householdName();
            }
            @Override public Apartment fromString(String value) { return null; }
        });
        apartment.getStyleClass().add("form-field");
        ComboBox<UtilityType> type = new ComboBox<>();
        type.getItems().setAll(UtilityType.values());
        type.setValue(current == null ? UtilityType.ELECTRICITY : current.type());
        type.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(UtilityType value) { return value == null ? "" : typeName(value); }
            @Override public UtilityType fromString(String value) { return UtilityType.ELECTRICITY; }
        });
        type.getStyleClass().add("form-field");
        ComboBox<Integer> month = new ComboBox<>();
        month.getItems().setAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        month.setValue(current == null ? LocalDate.now().getMonthValue() : current.month());
        month.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Integer value) { return value == null ? "" : "%02d".formatted(value); }
            @Override public Integer fromString(String value) { return null; }
        });
        month.getStyleClass().add("form-field");
        Spinner<Integer> year = new Spinner<>();
        year.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2000, 2200,
                current == null ? LocalDate.now().getYear() : current.year()));
        year.setEditable(false);
        year.getStyleClass().add("form-field");
        HBox period = new HBox(8, Ui.label("Tháng", "form-label"), month,
                Ui.label("Năm", "form-label"), year);
        TextField amount = Ui.textField("Ví dụ: 350000");
        TextField provider = Ui.textField("Tên đơn vị cung cấp");
        TextField reference = Ui.textField("Mã hóa đơn (không bắt buộc)");
        if (current != null) {
            apartment.setDisable(true);
            type.setDisable(true);
            month.setDisable(true);
            year.setDisable(true);
            amount.setText(current.amount().toPlainString());
            provider.setText(current.provider());
            reference.setText(current.reference() == null ? "" : current.reference());
        }

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        addRow(form, 0, "Kỳ hóa đơn", period);
        addRow(form, 1, "Căn hộ", apartment);
        addRow(form, 2, "Dịch vụ", type);
        addRow(form, 3, "Số tiền (VND)", amount);
        addRow(form, 4, "Nhà cung cấp", provider);
        addRow(form, 5, "Mã đối chiếu", reference);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(540);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                Apartment selectedApartment = apartment.getValue();
                services.utilities().save(user, new UtilityBillInput(current == null ? null : current.id(),
                        current == null ? year.getValue() : current.year(), month.getValue(),
                        selectedApartment == null ? 0 : selectedApartment.id(), type.getValue(),
                        new BigDecimal(amount.getText().trim()), provider.getText(), reference.getText()));
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không lưu được hóa đơn tiện ích", exception);
            }
        });
        dialog.showAndWait();
        load();
    }

    private void delete(UtilityBill bill) {
        if (bill.issued()) return;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa hóa đơn " + typeName(bill.type()) + " của căn hộ " + bill.apartmentCode() + "?",
                ButtonType.OK, ButtonType.CANCEL);
        confirmation.setTitle("Xóa hóa đơn tiện ích");
        confirmation.setHeaderText("Kỳ " + "%02d/%d".formatted(bill.month(), bill.year()));
        confirmation.initOwner(getScene().getWindow());
        if (confirmation.showAndWait().filter(ButtonType.OK::equals).isEmpty()) return;
        try {
            services.utilities().delete(user, bill.id());
            load();
        } catch (Exception exception) {
            Ui.error("Không xóa được hóa đơn tiện ích", exception);
        }
    }

    private static String typeName(UtilityType type) {
        return switch (type) {
            case ELECTRICITY -> "Điện";
            case WATER -> "Nước";
            case INTERNET -> "Internet";
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

    private static TableColumn<UtilityBill, String> column(String title, Function<UtilityBill, String> value) {
        TableColumn<UtilityBill, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
