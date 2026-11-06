package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
        TextField identifier = Ui.textField("Email hoặc số điện thoại");
        identifier.sceneProperty().addListener((observable, oldScene, scene) -> {
            if (scene != null) Platform.runLater(identifier::requestFocus);
        });
        Label identifierLabel = new Label("Email hoặc số điện thoại");
        identifierLabel.getStyleClass().add("form-label");
        identifierLabel.setLabelFor(identifier);
        PasswordField password = new PasswordField();
        password.setPromptText("Mật khẩu");
        password.getStyleClass().add("form-field");
        Label passwordLabel = new Label("Mật khẩu");
        passwordLabel.getStyleClass().add("form-label");
        passwordLabel.setLabelFor(password);
        Label error = new Label();
        error.getStyleClass().add("inline-error");
        error.setWrapText(true);
        Button submit = Ui.button("Đăng nhập", true);
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.setDefaultButton(true);
        submit.setOnAction(event -> signIn(identifier.getText(), password.getText(), error));
        identifier.setOnAction(event -> password.requestFocus());
        password.setOnAction(event -> signIn(identifier.getText(), password.getText(), error));
        Hyperlink forgotPassword = new Hyperlink("Quên mật khẩu?");
        forgotPassword.setOnAction(event -> showForgotPassword());

        panel.getChildren().addAll(logo, title, identifierLabel, identifier,
                passwordLabel, password, forgotPassword, error, submit);
        return panel;
    }

    private void signIn(String identifier, String password, Label error) {
        try {
            AppUser user = services.auth().login(identifier, password);
            if (user == null) {
                error.setText("Email, số điện thoại hoặc mật khẩu chưa đúng. Vui lòng kiểm tra lại.");
                return;
            }
            onLogin.accept(user);
        } catch (Exception exception) {
            error.setText(exception.getMessage() == null ? "Không thể đăng nhập." : exception.getMessage());
        }
    }

    private void showForgotPassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Quên mật khẩu");
        dialog.setHeaderText("Đặt lại mật khẩu");
        if (getScene() != null) dialog.initOwner(getScene().getWindow());
        ButtonType reset = new ButtonType("Đặt lại mật khẩu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(reset, ButtonType.CANCEL);

        TextField contact = Ui.textField("Email hoặc số điện thoại đã đăng ký");
        PasswordField password = new PasswordField();
        password.setPromptText("Mật khẩu mới");
        password.getStyleClass().add("form-field");
        PasswordField confirmation = new PasswordField();
        confirmation.setPromptText("Nhập lại mật khẩu mới");
        confirmation.getStyleClass().add("form-field");
        Label error = Ui.label("", "inline-error");
        error.setWrapText(true);

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        addRow(form, 0, "Email hoặc số điện thoại", contact);
        addRow(form, 1, "Mật khẩu mới", password);
        addRow(form, 2, "Xác nhận mật khẩu", confirmation);
        dialog.getDialogPane().setContent(new VBox(8, form, error));
        dialog.getDialogPane().setMinWidth(500);

        Button resetButton = (Button) dialog.getDialogPane().lookupButton(reset);
        resetButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                services.accounts().resetPassword(contact.getText(), password.getText(), confirmation.getText());
            } catch (Exception exception) {
                event.consume();
                error.setText(exception.getMessage() == null
                        ? "Không thể đặt lại mật khẩu. Vui lòng kiểm tra lại thông tin."
                        : exception.getMessage());
            }
        });
        if (dialog.showAndWait().filter(reset::equals).isPresent()) {
            Ui.info("Đã đổi mật khẩu", "Bạn có thể đăng nhập bằng mật khẩu mới.");
        }
    }

    private static void addRow(GridPane form, int row, String title, javafx.scene.Node field) {
        Label label = new Label(title);
        label.getStyleClass().add("form-label");
        label.setLabelFor(field);
        form.add(label, 0, row);
        form.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }
}
