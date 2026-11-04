package com.bluemoon.ui;

import com.bluemoon.model.Domain.Apartment;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.BillingPeriod;
import com.bluemoon.model.Domain.Gender;
import com.bluemoon.model.Domain.HouseholdRelationship;
import com.bluemoon.model.Domain.OccupancyStatus;
import com.bluemoon.model.Domain.Resident;
import com.bluemoon.model.Domain.ResidentDirectoryEntry;
import com.bluemoon.model.Domain.Role;
import com.bluemoon.service.HouseholdHeadExistsException;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
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
import java.util.List;
import java.util.function.Function;

final class ResidentView extends VBox {
    private final AppServices services;
    private final AppUser user;
    private final javafx.scene.control.TextField search = Ui.textField("Mã căn, họ tên, CCCD, điện thoại hoặc tầng");
    private final TableView<ResidentDirectoryEntry> table = new TableView<>();

    ResidentView(AppServices services, AppUser user) {
        this.services = services;
        this.user = user;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        var find = Ui.button("Tìm kiếm", true);
        find.setOnAction(event -> load());
        search.setOnAction(event -> load());
        var clear = Ui.button("Xóa lọc", false);
        clear.setOnAction(event -> {
            search.clear();
            load();
        });
        var details = Ui.button("Chi tiết căn hộ", false);
        details.setDisable(true);
        details.setOnAction(event -> showDetails());
        var edit = Ui.button("Sửa nhân khẩu", false);
        edit.setVisible(user.role() == Role.ADMIN);
        edit.setManaged(user.role() == Role.ADMIN);
        edit.setDisable(true);
        edit.setOnAction(event -> editSelected());
        Button add = Ui.button("Thêm nhân khẩu", true);
        add.setVisible(user.role() == Role.ADMIN);
        add.setManaged(user.role() == Role.ADMIN);
        add.setOnAction(event -> edit(null));
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, value) -> {
            details.setDisable(value == null);
            edit.setDisable(value == null || value.resident() == null);
        });
        getChildren().addAll(Ui.pageHeader("Cư dân", ""),
                Ui.toolbar(search, find, clear, details, edit, add), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        configureTable();
        load();
    }

    private void configureTable() {
        table.setPlaceholder(new Label("Không tìm thấy hồ sơ phù hợp."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Căn hộ", entry -> entry.apartment().code()));
        table.getColumns().add(column("Tầng", entry -> floorName(entry.apartment())));
        table.getColumns().add(column("Mã hộ khẩu", entry -> entry.apartment().householdCode()));
        table.getColumns().add(column("Họ tên", entry -> entry.resident() == null ? "Chưa đăng ký" : entry.resident().fullName()));
        table.getColumns().add(column("Quan hệ", entry -> entry.resident() == null ? "" : relationshipName(entry.resident().relationship())));
        table.getColumns().add(column("Ngày sinh", entry -> entry.resident() == null ? "" : Ui.date(entry.resident().dateOfBirth())));
        table.getColumns().add(column("CCCD / CMND", entry -> entry.resident() == null || entry.resident().nationalId() == null
                ? "" : entry.resident().nationalId()));
        table.getColumns().add(column("Liên hệ", entry -> entry.apartment().contactPhone()));
        table.getColumns().add(column("Tình trạng", entry -> occupancyName(entry.apartment().occupancyStatus())));
        table.setRowFactory(view -> {
            var row = new javafx.scene.control.TableRow<ResidentDirectoryEntry>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) showDetails(row.getItem());
            });
            return row;
        });
    }

    private void load() {
        try {
            table.getItems().setAll(services.residents().search(user, search.getText()));
        } catch (SQLException exception) {
            Ui.error("Không tải được hồ sơ cư dân", exception);
        }
    }

    private void editSelected() {
        ResidentDirectoryEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected != null && selected.resident() != null) edit(selected);
    }

    private void edit(ResidentDirectoryEntry selected) {
        List<Apartment> apartments;
        try {
            apartments = services.apartments().list(user);
        } catch (SQLException exception) {
            Ui.error("Không tải được danh sách căn hộ", exception);
            return;
        }
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(selected == null ? "Thêm nhân khẩu" : "Cập nhật nhân khẩu");
        dialog.setHeaderText("Thông tin nhân khẩu");
        dialog.initOwner(getScene().getWindow());
        ButtonType save = new ButtonType("Lưu hồ sơ", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);

        ComboBox<Apartment> apartment = new ComboBox<>();
        apartment.getItems().setAll(apartments);
        apartment.setPromptText("Chọn căn hộ");
        apartment.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Apartment value) {
                return value == null ? "" : value.code() + " · " + value.householdName();
            }
            @Override public Apartment fromString(String value) { return null; }
        });
        apartment.getStyleClass().add("form-field");
        var fullName = Ui.textField("Họ và tên");
        var dateOfBirth = new DatePicker(LocalDate.now().minusYears(18));
        dateOfBirth.getStyleClass().add("form-field");
        ComboBox<Gender> gender = new ComboBox<>();
        gender.getItems().setAll(Gender.values());
        gender.setPromptText("Chọn giới tính");
        gender.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Gender value) { return value == null ? "" : value == Gender.MALE ? "Nam" : "Nữ"; }
            @Override public Gender fromString(String value) { return Gender.FEMALE; }
        });
        gender.getStyleClass().add("form-field");
        var nationalId = Ui.textField("9 hoặc 12 chữ số; bắt buộc từ 14 tuổi");
        ComboBox<HouseholdRelationship> relationship = new ComboBox<>();
        relationship.getItems().setAll(HouseholdRelationship.values());
        relationship.setPromptText("Chọn quan hệ");
        relationship.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(HouseholdRelationship value) { return value == null ? "" : relationshipName(value); }
            @Override public HouseholdRelationship fromString(String value) { return HouseholdRelationship.OTHER; }
        });
        relationship.getStyleClass().add("form-field");
        var moveIn = new DatePicker(LocalDate.now());
        moveIn.getStyleClass().add("form-field");
        if (selected == null) {
            apartment.setValue(apartments.isEmpty() ? null : apartments.getFirst());
        } else {
            Resident resident = selected.resident();
            apartment.setValue(selected.apartment());
            apartment.setDisable(true);
            fullName.setText(resident.fullName());
            dateOfBirth.setValue(resident.dateOfBirth());
            gender.setValue(resident.gender());
            nationalId.setText(resident.nationalId() == null ? "" : resident.nationalId());
            relationship.setValue(resident.relationship());
            moveIn.setValue(resident.moveInDate());
        }

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        addRow(form, 0, "Căn hộ", apartment);
        addRow(form, 1, "Họ và tên", fullName);
        addRow(form, 2, "Ngày sinh", dateOfBirth);
        addRow(form, 3, "Giới tính", gender);
        addRow(form, 4, "CCCD / CMND", nationalId);
        addRow(form, 5, "Quan hệ với chủ hộ", relationship);
        addRow(form, 6, "Ngày chuyển đến", moveIn);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(540);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                Apartment selectedApartment = apartment.getValue();
                if (selectedApartment == null) throw new IllegalArgumentException("Chọn căn hộ.");
                Resident resident = new Resident(selected == null ? null : selected.resident().id(),
                        selectedApartment.id(), fullName.getText(), dateOfBirth.getValue(), gender.getValue(),
                        nationalId.getText(), relationship.getValue(), moveIn.getValue());
                try {
                    services.residents().save(user, resident, false);
                } catch (HouseholdHeadExistsException exception) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Thay đổi chủ hộ");
                    confirm.setHeaderText("Hộ đã có chủ hộ là " + exception.headName() + ".");
                    confirm.setContentText("Chuyển người này sang quan hệ Khác và đặt nhân khẩu mới làm chủ hộ?");
                    confirm.initOwner(getScene().getWindow());
                    if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        event.consume();
                        return;
                    }
                    services.residents().save(user, resident, true);
                }
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không lưu được hồ sơ nhân khẩu", exception);
            }
        });
        dialog.showAndWait();
        load();
    }

    private void showDetails() {
        ResidentDirectoryEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) showDetails(selected);
    }

    private void showDetails(ResidentDirectoryEntry selected) {
        try {
            Apartment apartment = selected.apartment();
            List<Resident> residents = services.residents().list(user, apartment.id());
            TableView<Resident> members = new TableView<>();
            members.setPlaceholder(new Label("Chưa có nhân khẩu trong căn hộ này."));
            members.setPrefHeight(210);
            members.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
            members.getColumns().add(column("Họ tên", Resident::fullName));
            members.getColumns().add(column("Giới tính", resident -> resident.gender() == Gender.MALE ? "Nam" : "Nữ"));
            members.getColumns().add(column("Ngày sinh", resident -> Ui.date(resident.dateOfBirth())));
            members.getColumns().add(column("Quan hệ", resident -> relationshipName(resident.relationship())));
            members.getColumns().add(column("CCCD / CMND", resident -> resident.nationalId() == null ? "" : resident.nationalId()));
            members.getColumns().add(column("Chuyển đến", resident -> Ui.date(resident.moveInDate())));
            members.getItems().setAll(residents);
            String headName = residents.stream().filter(resident -> resident.relationship() == HouseholdRelationship.HEAD)
                    .map(Resident::fullName).findFirst().orElse(apartment.householdName());
            Label title = Ui.label(apartment.code() + " · " + headName, "section-title");
            Label summary = Ui.label("Mã hộ " + apartment.householdCode() + " · Tầng " + floorName(apartment)
                    + " · " + apartment.areaM2().stripTrailingZeros().toPlainString() + " m² · "
                    + occupancyName(apartment.occupancyStatus()), "muted");
            Label phone = Ui.label("Liên hệ " + apartment.contactPhone() + " · Nhận nhà " + Ui.date(apartment.moveInDate()), "muted");
            Label debt = Ui.label(debtStatus(apartment), "section-title");
            VBox content = new VBox(12, title, summary, phone, members, debt);
            content.setPadding(new Insets(8, 0, 2, 0));
            Alert dialog = new Alert(Alert.AlertType.INFORMATION);
            dialog.setTitle("Hồ sơ căn hộ");
            dialog.setHeaderText("Thông tin căn hộ và nhân khẩu");
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().setMinWidth(850);
            dialog.initOwner(getScene().getWindow());
            dialog.showAndWait();
        } catch (SQLException exception) {
            Ui.error("Không tải được hồ sơ căn hộ", exception);
        }
    }

    private String debtStatus(Apartment apartment) throws SQLException {
        List<BillingPeriod> periods = services.billing().list(user);
        if (periods.isEmpty()) return "Chưa có kỳ thu";
        BillingPeriod latest = periods.getFirst();
        BigDecimal balance = services.collections().charges(user, latest.id(), apartment.code()).stream()
                .filter(charge -> charge.apartmentCode().equals(apartment.code()))
                .map(charge -> charge.balance()).reduce(BigDecimal.ZERO, BigDecimal::add);
        return "Công nợ kỳ " + latest.label() + ": " + Ui.money(balance);
    }

    private static String relationshipName(HouseholdRelationship relationship) {
        return switch (relationship) {
            case HEAD -> "Chủ hộ";
            case WIFE -> "Vợ";
            case HUSBAND -> "Chồng";
            case CHILD -> "Con";
            case PARENT -> "Bố/Mẹ";
            case TENANT -> "Người thuê";
            case OTHER -> "Khác";
        };
    }

    private static String occupancyName(OccupancyStatus status) {
        return switch (status) {
            case OCCUPIED -> "Đang ở";
            case VACANT -> "Căn hộ trống";
            case RENTED -> "Cho thuê";
        };
    }

    private static String floorName(Apartment apartment) {
        return apartment.floor() == 0 ? "Chưa xác định" : Integer.toString(apartment.floor());
    }

    private static void addRow(GridPane form, int row, String title, javafx.scene.Node field) {
        Label label = new Label(title);
        label.getStyleClass().add("form-label");
        label.setLabelFor(field);
        form.add(label, 0, row);
        form.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private static <T> TableColumn<T, String> column(String title, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
