package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Role;
import com.bluemoon.model.Domain.UserAccount;
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
                Ui.toolbar(Ui.label("Tài khoản kế toán được phép ghi nhận và tra cứu các khoản thu.", "muted"),
                        refresh, create), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setPlaceholder(new Label("Chưa có tài khoản."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.getColumns().add(column("Tên hiển thị", UserAccount::displayName));
        table.getColumns().add(column("Tên đăng nhập", UserAccount::username));
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
        var role = new ComboBox<Role>();
        role.getItems().setAll(Role.ADMIN, Role.ACCOUNTANT);
        role.setValue(Role.ACCOUNTANT);
        role.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Role value) { return roleName(value); }
            @Override public Role fromString(String value) { return Role.ACCOUNTANT; }
        });
        role.getStyleClass().add("form-field");
        var password = new PasswordField();
        password.setPromptText("Ít nhất 8 ký tự, tối đa 72 byte UTF-8");
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
        addRow(form, 2, "Vai trò", role);
        addRow(form, 3, "Mật khẩu", password);
        addRow(form, 4, "Xác nhận mật khẩu", confirmation);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(500);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                services.accounts().create(user, username.getText(), displayName.getText(), role.getValue(),
                        password.getText(), confirmation.getText());
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không tạo được tài khoản", exception);
            }
        });
        dialog.showAndWait();
        load();
    }

    private static String roleName(Role role) {
        return role == Role.ADMIN ? "Ban quản trị" : "Kế toán";
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
