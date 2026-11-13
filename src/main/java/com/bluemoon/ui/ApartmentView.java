package com.bluemoon.ui;

import com.bluemoon.model.Domain.Apartment;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.OccupancyStatus;
import com.bluemoon.model.Domain.Role;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
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
import java.util.function.Function;

final class ApartmentView extends VBox {
    private final AppServices services;
    private final AppUser user;
    private final javafx.scene.control.TextField search = Ui.textField("Tìm căn hộ, hộ khẩu, cư dân hoặc tầng");
    private final TableView<Apartment> table = new TableView<>();

    ApartmentView(AppServices services, AppUser user) {
        this.services = services;
        this.user = user;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        var add = Ui.button("Thêm căn hộ", true);
        add.setVisible(user.role() == Role.ADMIN);
        add.setManaged(user.role() == Role.ADMIN);
        add.setOnAction(event -> edit(null));
        var find = Ui.button("Tìm kiếm", true);
        find.setOnAction(event -> load());
        search.setOnAction(event -> load());
        var refresh = Ui.button("Xóa lọc", false);
        refresh.setOnAction(event -> {
            search.clear();
            load();
        });
        getChildren().addAll(Ui.pageHeader("Căn hộ", ""),
                Ui.toolbar(search, find, Ui.label("Diện tích mới áp dụng cho kỳ sau.", "muted"), refresh, add), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        load();
    }

    private void configureTable() {
        table.setPlaceholder(new Label("Chưa có căn hộ. Chọn Thêm căn hộ để bắt đầu."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Mã căn", Apartment::code));
        table.getColumns().add(column("Tầng", apartment -> apartment.floor() == 0 ? "Chưa xác định" : Integer.toString(apartment.floor())));
        table.getColumns().add(column("Mã hộ khẩu", Apartment::householdCode));
        table.getColumns().add(column("Diện tích", apartment -> apartment.areaM2().stripTrailingZeros().toPlainString() + " m²"));
        table.getColumns().add(column("Người liên hệ", Apartment::householdName));
        table.getColumns().add(column("Số điện thoại", Apartment::contactPhone));
        table.getColumns().add(column("Tình trạng", apartment -> statusName(apartment.occupancyStatus())));
        table.setRowFactory(view -> {
            TableRow<Apartment> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (user.role() == Role.ADMIN && event.getClickCount() == 2 && !row.isEmpty()) edit(row.getItem());
            });
            return row;
        });
    }

    private void load() {
        try {
            String term = search.getText() == null ? "" : search.getText().trim().toLowerCase(java.util.Locale.ROOT);
            List<Apartment> apartments = services.apartments().list(user).stream()
                    .filter(apartment -> term.isEmpty() || apartment.code().toLowerCase(java.util.Locale.ROOT).contains(term)
                            || apartment.householdCode().toLowerCase(java.util.Locale.ROOT).contains(term)
                            || apartment.householdName().toLowerCase(java.util.Locale.ROOT).contains(term)
                            || apartment.contactPhone().contains(term)
                            || Integer.toString(apartment.floor()).equals(term))
                    .toList();
            table.getItems().setAll(apartments);
        } catch (SQLException exception) {
            Ui.error("Không tải được danh sách căn hộ", exception);
        }
    }

    private void edit(Apartment current) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(current == null ? "Thêm căn hộ" : "Cập nhật căn hộ");
        dialog.setHeaderText("Thông tin căn hộ");
        dialog.initOwner(getScene().getWindow());
        ButtonType save = new ButtonType("Lưu thông tin", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);

        var code = Ui.textField("Ví dụ: P1002");
        var householdCode = Ui.textField("Ví dụ: HK-1002");
        var area = Ui.textField("Ví dụ: 65 hoặc 65,5");
        var household = Ui.textField("Tên người liên hệ");
        var phone = Ui.textField("Số điện thoại");
        var moveInDate = new DatePicker(LocalDate.now());
        moveInDate.getStyleClass().add("form-field");
        var occupancy = new ComboBox<OccupancyStatus>();
        occupancy.getItems().setAll(OccupancyStatus.values());
        occupancy.setValue(OccupancyStatus.OCCUPIED);
        occupancy.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(OccupancyStatus value) { return statusName(value); }
            @Override public OccupancyStatus fromString(String value) { return OccupancyStatus.OCCUPIED; }
        });
        occupancy.getStyleClass().add("form-field");
        var active = new CheckBox("Tính phí cho các kỳ mới");
        active.setSelected(true);
        if (current != null) {
            code.setText(current.code());
            householdCode.setText(current.householdCode());
            area.setText(current.areaM2().stripTrailingZeros().toPlainString());
            household.setText(current.householdName());
            phone.setText(current.contactPhone());
            moveInDate.setValue(current.moveInDate());
            occupancy.setValue(current.occupancyStatus());
            active.setSelected(current.active());
        }

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        addRow(form, 0, "Mã căn hộ", code);
        form.add(Ui.label("Tầng lấy từ phần số phía trước hai chữ số cuối của mã căn.", "muted"), 1, 1);
        addRow(form, 2, "Mã hộ khẩu", householdCode);
        addRow(form, 3, "Diện tích (m²)", area);
        addRow(form, 4, "Người liên hệ", household);
        addRow(form, 5, "Điện thoại", phone);
        addRow(form, 6, "Ngày nhận nhà", moveInDate);
        addRow(form, 7, "Tình trạng căn hộ", occupancy);
        form.add(active, 1, 8);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(520);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                BigDecimal areaValue = new BigDecimal(area.getText().trim().replace(',', '.'));
                services.apartments().save(user, new Apartment(current == null ? null : current.id(), code.getText(),
                        householdCode.getText(), areaValue, household.getText(), phone.getText(), moveInDate.getValue(),
                        occupancy.getValue(), active.isSelected()));
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không lưu được căn hộ", exception);
            }
        });
        dialog.showAndWait();
        load();
    }

    private static String statusName(OccupancyStatus status) {
        return switch (status) {
            case OCCUPIED -> "Đang ở";
            case VACANT -> "Căn hộ trống";
            case RENTED -> "Cho thuê";
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

    private static TableColumn<Apartment, String> column(String title, Function<Apartment, String> value) {
        TableColumn<Apartment, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
