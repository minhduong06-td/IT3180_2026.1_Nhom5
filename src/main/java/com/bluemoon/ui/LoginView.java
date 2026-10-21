package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
        HBox layout = new HBox(0, brandPanel(), loginPanel());
        layout.setMaxWidth(1020);
        layout.setMaxHeight(640);
        layout.getStyleClass().add("login-frame");
        getChildren().add(layout);
        StackPane.setAlignment(layout, Pos.CENTER);
        StackPane.setMargin(layout, new Insets(28));
    }

    private VBox brandPanel() {
        VBox panel = new VBox(22);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(54));
        panel.getStyleClass().add("login-brand-panel");
        HBox logo = new HBox(12, new MoonMark(), new Label("BlueMoon"));
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.getStyleClass().add("brand-lockup");
        Label headline = new Label("Một nhịp thu phí\nrõ ràng cho cả tòa nhà.");
        headline.getStyleClass().add("login-headline");
        Label copy = new Label("Theo dõi khoản thu, công nợ và lịch sử thanh toán trong cùng một nơi.");
        copy.getStyleClass().add("login-copy");
        copy.setWrapText(true);
        Label footer = new Label("BLUE MOON RESIDENCE  ·  VẬN HÀNH MINH BẠCH");
        footer.getStyleClass().add("login-footnote");
        VBox.setVgrow(copy, Priority.ALWAYS);
        panel.getChildren().addAll(logo, headline, copy, footer);
        return panel;
    }

    private VBox loginPanel() {
        VBox panel = new VBox(16);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(52, 58, 46, 58));
        panel.getStyleClass().add("login-form-panel");
        Label title = new Label("Đăng nhập");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Dành cho ban quản trị và kế toán.");
        subtitle.getStyleClass().add("page-subtitle");
        TextField username = Ui.textField("Tên tài khoản");
        Label usernameLabel = new Label("Tên tài khoản");
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
        var submit = Ui.button("Tiếp tục", true);
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setDefaultButton(true);
        submit.setOnAction(event -> signIn(username.getText(), password.getText(), error));
        username.setOnAction(event -> password.requestFocus());
        password.setOnAction(event -> signIn(username.getText(), password.getText(), error));

        panel.getChildren().addAll(title, subtitle, usernameLabel, username,
                passwordLabel, password, error, submit);
        return panel;
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
