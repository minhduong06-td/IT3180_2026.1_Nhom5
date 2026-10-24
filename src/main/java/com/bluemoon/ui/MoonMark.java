package com.bluemoon.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

final class MoonMark extends StackPane {
    MoonMark() {
        setPrefSize(36, 36);
        Circle moon = new Circle(13);
        moon.getStyleClass().add("moon-mark");
        Circle cutout = new Circle(11);
        cutout.setTranslateX(7);
        cutout.setTranslateY(-5);
        cutout.getStyleClass().add("moon-cutout");
        getChildren().addAll(moon, cutout);
    }
}
