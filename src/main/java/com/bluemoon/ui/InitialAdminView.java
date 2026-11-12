package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.service.AccountService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

final class InitialAdminView extends StackPane {
    private final AccountService accounts;
    private final Consumer<AppUser> onCreated;
    private final TextField username = Ui.textField("Tên đăng nhập");
    private final TextField displayName = Ui.textField("Tên hiển thị");
    private final PasswordField password = new PasswordField();
    private final PasswordField confirmation = new PasswordField();
    private final Label error = new Label();

    InitialAdminView(AccountService accounts, Consumer<AppUser> onCreated) {
        this.accounts = accounts;
        this.onCreated = onCreated;
        getStyleClass().add("login-background");

        Label brand = new Label("BlueMoon");
        brand.getStyleClass().add("brand-title");
        Label title = new Label("Tạo tài khoản quản trị");
        title.getStyleClass().add("page-title");

        password.setPromptText("Ít nhất 8 ký tự, tối đa 72 byte UTF-8");
        password.getStyleClass().add("form-field");
        confirmation.setPromptText("Nhập lại mật khẩu");
        confirmation.getStyleClass().add("form-field");
        error.getStyleClass().add("inline-error");
        error.setWrapText(true);

        var create = Ui.button("Tạo tài khoản và tiếp tục", true);
        create.setMaxWidth(Double.MAX_VALUE);
        create.setDefaultButton(true);
        create.setOnAction(event -> createAccount());
        confirmation.setOnAction(event -> createAccount());

        VBox panel = new VBox(16, brand, title,
                field("Tên đăng nhập", username), field("Tên hiển thị", displayName),
                field("Mật khẩu", password), field("Xác nhận mật khẩu", confirmation), error, create);
        panel.setPadding(new Insets(40));
        panel.setMaxWidth(440);
        panel.setMaxHeight(Region.USE_PREF_SIZE);
        panel.getStyleClass().add("login-card");
        panel.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(panel);
    }

    private static VBox field(String title, javafx.scene.control.Control control) {
        Label label = new Label(title);
        label.getStyleClass().add("form-label");
        label.setLabelFor(control);
        return new VBox(6, label, control);
    }

    private void createAccount() {
        try {
            AppUser user = accounts.createInitialAdmin(username.getText(), displayName.getText(),
                    password.getText(), confirmation.getText());
            onCreated.accept(user);
        } catch (Exception exception) {
            error.setText(exception.getMessage() == null ? "Không tạo được tài khoản quản trị." : exception.getMessage());
        }
    }
}
