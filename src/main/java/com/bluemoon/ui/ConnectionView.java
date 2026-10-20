package com.bluemoon.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

final class ConnectionView extends VBox {
    ConnectionView(String title, Throwable error, Runnable retry) {
        setAlignment(Pos.CENTER);
        setSpacing(14);
        getStyleClass().add("connection-view");
        Label brand = new Label("BlueMoon");
        brand.getStyleClass().add("brand-title");
        Label heading = new Label(title);
        heading.getStyleClass().add("page-title");
        Label message = new Label(error == null
                ? "Đang chuẩn bị dữ liệu khởi chạy."
                : "Hãy kiểm tra MySQL đang chạy và cấu hình kết nối trong README.\n" + error.getMessage());
        message.setWrapText(true);
        message.getStyleClass().add("page-subtitle");
        getChildren().addAll(brand, heading, message);
        if (error != null) {
            Button button = Ui.button("Thử kết nối lại", true);
            button.setOnAction(event -> retry.run());
            getChildren().add(button);
        }
    }
}
