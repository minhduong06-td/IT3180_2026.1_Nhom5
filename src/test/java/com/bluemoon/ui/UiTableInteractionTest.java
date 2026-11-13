package com.bluemoon.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class UiTableInteractionTest {
    private static final List<String> TABLE_VIEWS = List.of(
            "ApartmentView.java", "BillingView.java", "ChargesView.java", "FeeView.java",
            "ResidentRequestView.java", "ResidentView.java", "UtilityView.java", "VehicleView.java");

    @Test
    void operationalTablesDoNotRequireSelectingRows() throws IOException {
        for (String file : TABLE_VIEWS) {
            String source = Files.readString(Path.of("src", "main", "java", "com", "bluemoon", "ui", file));
            assertFalse(source.contains("selectedItemProperty"), file + " vẫn phụ thuộc vào việc chọn dòng");
            assertFalse(source.contains("getSelectionModel().getSelectedItem()"), file + " vẫn đọc dòng đang chọn");
            assertFalse(source.contains("setRowFactory"), file + " vẫn dùng tương tác ngầm trên dòng");
        }
    }
}
