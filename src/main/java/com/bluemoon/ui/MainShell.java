package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Role;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Supplier;

final class MainShell extends BorderPane {
    private final AppServices services;
    private final AppUser user;
    private final Runnable logout;
    private final ToggleGroup navigation = new ToggleGroup();
    private final VBox navItems = new VBox(5);

    MainShell(AppServices services, AppUser user, Runnable logout) {
        this.services = services;
        this.user = user;
        this.logout = logout;
        getStyleClass().add("app-shell");
        setLeft(sidebar());
        setTop(topbar());
    }

    private VBox sidebar() {
        VBox sidebar = new VBox(14);
        sidebar.getStyleClass().add("app-sidebar");
        HBox brand = new HBox(10, new MoonMark(), new Label("BlueMoon"));
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.getStyleClass().add("sidebar-brand");
        navItems.getStyleClass().add("sidebar-nav");
        addNavigation("Tổng quan", () -> new DashboardView(services, user, () -> select("Kỳ thu")));
        if (user.role() == Role.ADMIN) {
            sidebar.getChildren().addAll(brand, Ui.label("VẬN HÀNH", "sidebar-caption"), navItems);
            addNavigation("Căn hộ", () -> new ApartmentView(services, user));
            addNavigation("Cư dân", () -> new ResidentView(services, user));
            addNavigation("Khoản thu", () -> new FeeView(services, user));
            addNavigation("Tài khoản", () -> new AccountView(services, user));
            addNavigation("Kỳ thu", () -> new BillingView(services, user, this::showCharges));
        } else {
            sidebar.getChildren().addAll(brand, Ui.label("THU PHÍ", "sidebar-caption"), navItems);
            addNavigation("Cư dân", () -> new ResidentView(services, user));
        }
        addNavigation("Bảng kê phải thu", () -> new ChargesView(services, user));
        addNavigation("Lịch sử thu", () -> new TransactionsView(services, user));
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        VBox account = new VBox(5, Ui.label(user.displayName(), ""),
                Ui.label(user.role() == Role.ADMIN ? "Ban quản trị" : "Kế toán", "sidebar-role"));
        account.getStyleClass().add("sidebar-user");
        sidebar.getChildren().addAll(spacer, account);
        return sidebar;
    }

    private HBox topbar() {
        Label title = new Label("Quản lý thu phí BlueMoon");
        title.getStyleClass().add("topbar-title");
        Region spacer = Ui.spacer();
        var changePassword = Ui.button("Đổi mật khẩu", false);
        changePassword.setOnAction(event -> changePassword());
        var signOut = Ui.button("Đăng xuất", false);
        signOut.setOnAction(event -> logout.run());
        HBox bar = new HBox(12, title, spacer, changePassword, signOut);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("app-topbar");
        return bar;
    }

    private void changePassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Cập nhật mật khẩu đăng nhập");
        dialog.initOwner(getScene().getWindow());
        var save = new ButtonType("Cập nhật", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(save, ButtonType.CANCEL);
        var current = new PasswordField();
        current.setPromptText("Mật khẩu hiện tại");
        current.getStyleClass().add("form-field");
        var next = new PasswordField();
        next.setPromptText("Mật khẩu mới từ 8 đến 72 byte");
        next.getStyleClass().add("form-field");
        var confirmation = new PasswordField();
        confirmation.setPromptText("Nhập lại mật khẩu mới");
        confirmation.getStyleClass().add("form-field");
        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(8, 0, 4, 0));
        addPasswordRow(form, 0, "Mật khẩu hiện tại", current);
        addPasswordRow(form, 1, "Mật khẩu mới", next);
        addPasswordRow(form, 2, "Xác nhận mật khẩu", confirmation);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setMinWidth(480);
        var saveButton = (Button) dialog.getDialogPane().lookupButton(save);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                services.accounts().changePassword(user, current.getText(), next.getText(), confirmation.getText());
            } catch (Exception exception) {
                event.consume();
                Ui.error("Không đổi được mật khẩu", exception);
            }
        });
        if (dialog.showAndWait().filter(save::equals).isPresent()) {
            Ui.info("Đã đổi mật khẩu", "Mật khẩu đăng nhập đã được cập nhật.");
        }
    }

    private static void addPasswordRow(GridPane form, int row, String title, Node field) {
        var label = new Label(title);
        label.getStyleClass().add("form-label");
        label.setLabelFor(field);
        form.add(label, 0, row);
        form.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
    }

    private void addNavigation(String title, Supplier<Node> page) {
        ToggleButton button = new ToggleButton(title);
        button.setToggleGroup(navigation);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("nav-button");
        button.setOnAction(event -> setCenter(page.get()));
        button.setUserData(title);
        navItems.getChildren().add(button);
        if ("Tổng quan".equals(title)) {
            button.setSelected(true);
            setCenter(page.get());
        }
    }

    private void select(String label) {
        navItems.getChildren().stream().filter(ToggleButton.class::isInstance).map(ToggleButton.class::cast)
                .filter(button -> label.equals(button.getUserData())).findFirst().ifPresent(ToggleButton::fire);
    }

    private void showCharges(long periodId) {
        setCenter(new ChargesView(services, user, periodId));
        navItems.getChildren().stream().filter(ToggleButton.class::isInstance).map(ToggleButton.class::cast)
                .filter(button -> "Bảng kê phải thu".equals(button.getUserData()))
                .findFirst().ifPresent(button -> button.setSelected(true));
    }
}
