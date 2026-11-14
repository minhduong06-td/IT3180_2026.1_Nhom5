package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Apartment;
import com.bluemoon.model.Domain.Role;
import com.bluemoon.model.Domain.UserAccount;
import com.bluemoon.model.RoleLabels;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.Function;

final class AccountView extends VBox {
    private final AppServices services;
    private final AppUser user;
    private final TableView<UserAccount> table = new TableView<>();

    AccountView(AppServices services, AppUser user) {
        this.services = services;
        this.user = user;
        setSpacing(18);
        setPadding(new Insets(28, 32, 32, 32));
        getStyleClass().add("page");
        var create = Ui.button("Tạo tài khoản", true);
        create.setOnAction(event -> create());
        var refresh = Ui.button("Làm mới", false);
        refresh.setOnAction(event -> load());
        getChildren().addAll(Ui.pageHeader("Tài khoản", ""),
                Ui.toolbar(refresh, create), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setPlaceholder(new Label("Chưa có tài khoản."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setSelectionModel(null);
        table.getColumns().add(column("Tên hiển thị", UserAccount::displayName));
        table.getColumns().add(column("Tên tài khoản", UserAccount::username));
        table.getColumns().add(column("Email", account -> account.email() == null ? "" : account.email()));
        table.getColumns().add(column("Số điện thoại", account -> account.phone() == null ? "" : account.phone()));
        table.getColumns().add(column("Vai trò", account -> roleName(account.role())));
        table.getColumns().add(column("Trạng thái", account -> !account.active() ? "Đã ngừng" :
                account.loginLocked() ? "Tạm khóa" : "Đang hoạt động"));
        load();
    }

    private void load() {
        try {
            table.getItems().setAll(services.accounts().list(user));
        } catch (Exception exception) {
            Ui.error("Không tải được danh sách tài khoản", exception);
        }
    }

    private void create() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tạo tài khoản");
        dialog.setHeaderText("Thông tin đăng nhập và phân quyền");
        dialog.initOwner(getScene().getWindow());
        ButtonType save = new ButtonType("Tạo tài khoản", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);
        var displayName = Ui.textField("Họ và tên");
        var username = Ui.textField("Tên đăng nhập");
        var email = Ui.textField("Email");
        var phone = Ui.textField("Số điện thoại");
        var role = new ComboBox<Role>();
        role.getItems().setAll(Role.ADMIN, Role.ACCOUNTANT, Role.RESIDENT);
        role.setValue(Role.ACCOUNTANT);
        role.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Role value) { return roleName(value); }
            @Override public Role fromString(String value) { return Role.ACCOUNTANT; }
        });
        role.getStyleClass().add("form-field");
        ComboBox<Apartment> apartment = new ComboBox<>();
        apartment.getStyleClass().add("form-field");
        apartment.setPromptText("Chọn căn hộ của người dùng");
        apartment.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Apartment value) {
                return value == null ? "" : value.code() + " · " + value.householdName();
            }
            @Override public Apartment fromString(String value) { return null; }
        });
        try {
            apartment.getItems().setAll(services.apartments().list(user).stream().filter(Apartment::active).toList());
        } catch (Exception exception) {
            Ui.error("Không tải được danh sách căn hộ", exception);
        }
        Runnable updateApartmentVisibility = () -> {
            boolean resident = role.getValue() == Role.RESIDENT;
            apartment.setVisible(resident);
            apartment.setManaged(resident);
            if (!resident) apartment.getSelectionModel().clearSelection();
        };
        role.valueProperty().addListener((observable, oldValue, newValue) -> updateApartmentVisibility.run());
        updateApartmentVisibility.run();
        var password = new PasswordField();
        password.setPromptText("Mật khẩu");
        password.getStyleClass().add("form-field");
        var confirmation = new PasswordField();
        confirmation.setPromptText("Nhập lại mật khẩu");
        confirmation.getStyleClass().add("form-field");
        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        addRow(form, 0, "Tên hiển thị", displayName);
        addRow(form, 1, "Tên đăng nhập", username);
        addRow(form, 2, "Email", email);
        addRow(form, 3, "Số điện thoại", phone);
        addRow(form, 4, "Vai trò", role);
        addRow(form, 5, "Căn hộ", apartment);
        addRow(form, 6, "Mật khẩu", password);
        addRow(form, 7, "Xác nhận mật khẩu", confirmation);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(500);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                services.accounts().create(user, username.getText(), displayName.getText(), role.getValue(),
                        apartment.getValue() == null ? null : apartment.getValue().id(), email.getText(),
                        phone.getText(), password.getText(), confirmation.getText());
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không tạo được tài khoản", exception);
            }
        });
        dialog.showAndWait();
        load();
    }

    private static String roleName(Role role) {
        return role == null ? "" : RoleLabels.displayName(role);
    }

    private static void addRow(GridPane form, int row, String title, javafx.scene.Node field) {
        Label label = new Label(title);
        label.getStyleClass().add("form-label");
        label.setLabelFor(field);
        form.add(label, 0, row);
        form.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private static TableColumn<UserAccount, String> column(String title, Function<UserAccount, String> value) {
        TableColumn<UserAccount, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(value.apply(cell.getValue())));
        return column;
    }
}
