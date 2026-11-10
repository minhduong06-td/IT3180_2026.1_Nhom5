package com.bluemoon.ui;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

final class MoonMark extends StackPane {
    MoonMark() {
        setPrefSize(36, 36);
        Circle moon = new Circle(13);
        Circle cutout = new Circle(11);
        cutout.setTranslateX(7);
        cutout.setTranslateY(-5);
        Shape crescent = Shape.subtract(moon, cutout);
        crescent.getStyleClass().add("moon-mark");
        getChildren().add(crescent);
    }
}
