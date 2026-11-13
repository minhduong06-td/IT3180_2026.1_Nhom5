package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

final class LoginView extends StackPane {
    private final AppServices services;
    private final Consumer<AppUser> onLogin;

    LoginView(AppServices services, Consumer<AppUser> onLogin) {
        this.services = services;
        this.onLogin = onLogin;
        getStyleClass().add("login-background");
        VBox panel = loginPanel();
        panel.setMaxWidth(440);
        panel.setMaxHeight(Region.USE_PREF_SIZE);
        panel.getStyleClass().add("login-card");
        getChildren().add(panel);
        StackPane.setAlignment(panel, Pos.CENTER);
        StackPane.setMargin(panel, new Insets(20));
    }

    private VBox loginPanel() {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(36));
        HBox logo = new HBox(10, new MoonMark(), new Label("BlueMoon"));
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.getStyleClass().add("login-logo");
        Label title = new Label("Đăng nhập");
        title.getStyleClass().add("page-title");
        TextField username = Ui.textField("Tên đăng nhập");
        username.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene != null) Platform.runLater(username::requestFocus);
        });
        Label usernameLabel = new Label("Tên đăng nhập");
        usernameLabel.getStyleClass().add("form-label");
        usernameLabel.setLabelFor(username);
        PasswordField password = new PasswordField();
        password.setPromptText("Mật khẩu");
        password.getStyleClass().add("form-field");
        Label passwordLabel = new Label("Mật khẩu");
        passwordLabel.getStyleClass().add("form-label");
        passwordLabel.setLabelFor(password);
        Label error = new Label();
        error.getStyleClass().add("inline-error");
        error.setWrapText(true);
        var submit = Ui.button("Đăng nhập", true);
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setDefaultButton(true);
        submit.setOnAction(event -> signIn(username.getText(), password.getText(), error));
        username.setOnAction(event -> password.requestFocus());
        password.setOnAction(event -> signIn(username.getText(), password.getText(), error));

        HBox demoAccounts = new HBox(8);
        demoAccounts.getStyleClass().add("demo-accounts");
        Label demoLabel = new Label("Dùng nhanh");
        demoLabel.getStyleClass().add("muted");
        var adminDemo = Ui.button("Ban quản trị", false);
        var accountantDemo = Ui.button("Kế toán", false);
        var residentDemo = Ui.button("Người dùng", false);
        adminDemo.setOnAction(event -> fillDemo(username, password, "admin", "admin123"));
        accountantDemo.setOnAction(event -> fillDemo(username, password, "accountant", "accountant123"));
        residentDemo.setOnAction(event -> fillDemo(username, password, "resident", "resident123"));
        demoAccounts.getChildren().addAll(demoLabel, adminDemo, accountantDemo, residentDemo);

        panel.getChildren().addAll(logo, title, usernameLabel, username,
                passwordLabel, password, demoAccounts, error, submit);
        return panel;
    }

    private static void fillDemo(TextField username, PasswordField password, String demoUsername, String demoPassword) {
        username.setText(demoUsername);
        password.setText(demoPassword);
        password.requestFocus();
    }

    private void signIn(String username, String password, Label error) {
        try {
            AppUser user = services.auth().login(username, password);
            if (user == null) {
                error.setText("Tên đăng nhập hoặc mật khẩu chưa đúng. Vui lòng kiểm tra lại.");
                return;
            }
            onLogin.accept(user);
        } catch (Exception exception) {
            error.setText(exception.getMessage() == null ? "Không thể đăng nhập." : exception.getMessage());
        }
    }
}
