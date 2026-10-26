package com.bluemoon.ui;

import com.bluemoon.model.Domain.Apartment;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Vehicle;
import com.bluemoon.model.Domain.VehicleType;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

final class VehicleView extends VBox {
    private final AppServices services;
    private final AppUser user;
    private final TextField search = Ui.textField("Tìm biển số, căn hộ hoặc chủ hộ");
    private final TableView<Vehicle> table = new TableView<>();

    VehicleView(AppServices services, AppUser user) {
        this.services = services;
        this.user = user;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        Button add = Ui.button("Đăng ký phương tiện", true);
        add.setOnAction(event -> register());
        Button deactivate = Ui.button("Ngừng đăng ký", false);
        deactivate.setDisable(true);
        deactivate.setOnAction(event -> deactivateSelected());
        Button refresh = Ui.button("Làm mới", false);
        refresh.setOnAction(event -> load());
        search.setOnAction(event -> load());
        getChildren().addAll(Ui.pageHeader("Phương tiện", "Thay đổi này chỉ ảnh hưởng các kỳ thu chưa phát hành."),
                Ui.toolbar(search, refresh, Ui.spacer(), deactivate, add), table);
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldVehicle, vehicle) ->
                deactivate.setDisable(vehicle == null || !vehicle.active()));
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        load();
    }

    private void configureTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Biển số", Vehicle::licensePlate));
        table.getColumns().add(column("Loại xe", vehicle -> typeName(vehicle.type())));
        table.getColumns().add(column("Căn hộ", Vehicle::apartmentCode));
        table.getColumns().add(column("Chủ hộ", Vehicle::householdName));
        table.getColumns().add(column("Trạng thái", vehicle -> vehicle.active() ? "Đang đăng ký" : "Đã ngừng"));
    }

    private void load() {
        try {
            String term = search.getText() == null ? "" : search.getText().trim().toLowerCase(Locale.ROOT);
            List<Vehicle> vehicles = services.vehicles().list(user).stream()
                    .filter(vehicle -> term.isEmpty() || vehicle.licensePlate().toLowerCase(Locale.ROOT).contains(term)
                            || vehicle.apartmentCode().toLowerCase(Locale.ROOT).contains(term)
                            || vehicle.householdName().toLowerCase(Locale.ROOT).contains(term))
                    .toList();
            table.getItems().setAll(vehicles);
        } catch (SQLException exception) {
            Ui.error("Không tải được danh sách phương tiện", exception);
        }
    }

    private void register() {
        List<Apartment> apartments;
        try {
            apartments = services.apartments().list(user).stream().filter(Apartment::active).toList();
        } catch (SQLException exception) {
            Ui.error("Không tải được danh sách căn hộ", exception);
            return;
        }
        if (apartments.isEmpty()) {
            Ui.info("Chưa có căn hộ hoạt động", "Thêm hoặc kích hoạt căn hộ trước khi đăng ký phương tiện.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đăng ký phương tiện");
        dialog.setHeaderText("Thông tin phương tiện của hộ gia đình");
        dialog.initOwner(getScene().getWindow());
        ButtonType save = new ButtonType("Lưu đăng ký", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);

        ComboBox<Apartment> apartment = new ComboBox<>();
        apartment.getItems().setAll(apartments);
        apartment.setValue(apartments.getFirst());
        apartment.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Apartment value) {
                return value == null ? "" : value.code() + " · " + value.householdName();
            }
            @Override public Apartment fromString(String value) { return null; }
        });
        apartment.getStyleClass().add("form-field");
        ComboBox<VehicleType> type = new ComboBox<>();
        type.getItems().setAll(VehicleType.values());
        type.setValue(VehicleType.MOTORCYCLE);
        type.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(VehicleType value) { return value == null ? "" : typeName(value); }
            @Override public VehicleType fromString(String value) { return VehicleType.MOTORCYCLE; }
        });
        type.getStyleClass().add("form-field");
        TextField plate = Ui.textField("Ví dụ: 30A-12345");

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        addRow(form, 0, "Căn hộ", apartment);
        addRow(form, 1, "Loại xe", type);
        addRow(form, 2, "Biển số", plate);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(500);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                Apartment selected = apartment.getValue();
                services.vehicles().register(user, selected == null ? 0 : selected.id(), type.getValue(), plate.getText());
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không đăng ký được phương tiện", exception);
            }
        });
        dialog.showAndWait();
        load();
    }

    private void deactivateSelected() {
        Vehicle vehicle = table.getSelectionModel().getSelectedItem();
        if (vehicle == null || !vehicle.active()) return;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Ngừng tính phí phương tiện này từ các kỳ thu được phát hành sau đó?",
                ButtonType.OK, ButtonType.CANCEL);
        confirmation.setTitle("Ngừng đăng ký phương tiện");
        confirmation.setHeaderText(vehicle.licensePlate() + " · " + vehicle.apartmentCode());
        confirmation.initOwner(getScene().getWindow());
        if (confirmation.showAndWait().filter(ButtonType.OK::equals).isEmpty()) return;
        try {
            services.vehicles().deactivate(user, vehicle.id());
            load();
        } catch (Exception exception) {
            Ui.error("Không ngừng đăng ký được phương tiện", exception);
        }
    }

    private static String typeName(VehicleType type) {
        return type == VehicleType.MOTORCYCLE ? "Xe máy" : "Ô tô";
    }

    private static void addRow(GridPane grid, int row, String title, javafx.scene.Node field) {
        Label label = new Label(title);
        label.getStyleClass().add("form-label");
        label.setLabelFor(field);
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private static TableColumn<Vehicle, String> column(String title, Function<Vehicle, String> value) {
        TableColumn<Vehicle, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
