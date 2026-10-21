package com.bluemoon.ui;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Role;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
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
            addNavigation("Khoản thu", () -> new FeeView(services, user));
            addNavigation("Kỳ thu", () -> new BillingView(services, user, this::showCharges));
        } else {
            sidebar.getChildren().addAll(brand, Ui.label("THU PHÍ", "sidebar-caption"), navItems);
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
        var signOut = Ui.button("Đăng xuất", false);
        signOut.setOnAction(event -> logout.run());
        HBox bar = new HBox(16, title, spacer, signOut);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("app-topbar");
        return bar;
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
