package com.bluemoon.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class Ui {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Ui() {}

    static VBox page(String title, String subtitle, Node... content) {
        Label heading = new Label(title);
        heading.getStyleClass().add("page-title");
        Label intro = new Label(subtitle);
        intro.getStyleClass().add("page-subtitle");
        VBox box = new VBox(20, new VBox(4, heading, intro));
        box.getStyleClass().add("page");
        box.getChildren().addAll(content);
        return box;
    }

    static VBox pageHeader(String title, String subtitle) {
        return new VBox(4, label(title, "page-title"), label(subtitle, "page-subtitle"));
    }

    static Label label(String value, String style) {
        Label label = new Label(value);
        if (style != null && !style.isBlank()) label.getStyleClass().add(style);
        return label;
    }

    static Button button(String text, boolean primary) {
        Button button = new Button(text);
        button.getStyleClass().add(primary ? "button-primary" : "button-secondary");
        button.setFocusTraversable(true);
        return button;
    }

    static TextField textField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("form-field");
        return field;
    }

    static Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    static String money(BigDecimal value) {
        return CURRENCY.format(value == null ? BigDecimal.ZERO : value);
    }

    static String date(LocalDate value) {
        return value == null ? "—" : DATE.format(value);
    }

    static void error(String title, Throwable error) {
        String message = error.getMessage();
        if (error instanceof java.sql.SQLIntegrityConstraintViolationException
                || (error instanceof java.sql.SQLException sql && "23000".equals(sql.getSQLState()))) {
            message = "Dữ liệu đã tồn tại hoặc đang được sử dụng trong kỳ thu.";
        } else if (message == null || message.isBlank()) {
            message = "Đã có lỗi xảy ra. Vui lòng thử lại.";
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    static void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    static HBox toolbar(Node... nodes) {
        HBox box = new HBox(12, nodes);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(0, 0, 4, 0));
        return box;
    }
}
