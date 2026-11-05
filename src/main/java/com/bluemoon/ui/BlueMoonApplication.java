package com.bluemoon.ui;

import com.bluemoon.config.Database;
import com.bluemoon.config.DatabaseBootstrap;
import com.bluemoon.dao.AppRepository;
import com.bluemoon.service.ApartmentService;
import com.bluemoon.service.AccountService;
import com.bluemoon.service.AuthService;
import com.bluemoon.service.BillingService;
import com.bluemoon.service.CollectionService;
import com.bluemoon.service.ContributionService;
import com.bluemoon.service.DashboardService;
import com.bluemoon.service.FeeService;
import com.bluemoon.service.ResidentService;
import com.bluemoon.service.ResidentRequestService;
import com.bluemoon.service.VehicleService;
import com.bluemoon.service.UtilityService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public final class BlueMoonApplication extends Application {
    private Stage stage;
    private AppServices services;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Database database = new Database();
        AppRepository repository = new AppRepository(database);
        services = new AppServices(new AuthService(repository), new AccountService(repository),
                new ApartmentService(repository), new ResidentService(repository),
                new FeeService(repository), new BillingService(repository),
                new CollectionService(repository), new ContributionService(repository),
                new DashboardService(repository), new VehicleService(repository), new UtilityService(repository),
                new ResidentRequestService(repository));
        stage.setTitle("BlueMoon | Quản lý thu phí");
        stage.setWidth(1280);
        stage.setHeight(820);
        stage.setMinWidth(1080);
        stage.setMinHeight(700);
        Scene scene = new Scene(new StackPane());
        scene.getStylesheets().add(getClass().getResource("/ui/app.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        connect();
    }

    private void connect() {
        setRoot(new ConnectionView("Đang kết nối cơ sở dữ liệu...", null, this::connect));
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                new DatabaseBootstrap(new Database()).initialize();
                return null;
            }
        };
        task.setOnSucceeded(event -> setRoot(new LoginView(services, this::openApp)));
        task.setOnFailed(event -> setRoot(new ConnectionView(
                "Không kết nối được MySQL", task.getException(), this::connect)));
        Thread thread = new Thread(task, "bluemoon-database-bootstrap");
        thread.setDaemon(true);
        thread.start();
    }

    private void openApp(com.bluemoon.model.Domain.AppUser user) {
        setRoot(new MainShell(services, user, this::showLogin));
    }

    private void showLogin() {
        setRoot(new LoginView(services, this::openApp));
    }

    private void setRoot(javafx.scene.Parent root) {
        Platform.runLater(() -> stage.getScene().setRoot(root));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
