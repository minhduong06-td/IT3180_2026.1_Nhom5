package com.bluemoon.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

final class CsvExporter {
    private CsvExporter() {}

    static void save(Window owner, String title, String fileName, List<String> headers,
                     List<List<String>> rows) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialFileName(fileName);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Tệp CSV (*.csv)", "*.csv"));
        File selected = chooser.showSaveDialog(owner);
        if (selected == null) return;
        File target = selected.getName().toLowerCase(Locale.ROOT).endsWith(".csv")
                ? selected : new File(selected.getParentFile(), selected.getName() + ".csv");
        if (target.exists()) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                    "Tệp đã tồn tại. Ghi đè tệp này?", ButtonType.OK, ButtonType.CANCEL);
            confirmation.setTitle("Xác nhận ghi đè");
            confirmation.setHeaderText(target.getName());
            confirmation.initOwner(owner);
            if (confirmation.showAndWait().filter(ButtonType.OK::equals).isEmpty()) return;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(target.toPath(), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writer.write("sep=,");
            writer.write("\r\n");
            writeRow(writer, headers);
            for (List<String> row : rows) writeRow(writer, row);
        } catch (IOException exception) {
            Ui.error("Không lưu được tệp CSV", exception);
            return;
        }
        Ui.info("Đã xuất tệp CSV", "Tệp đã được lưu tại:\n" + target.getAbsolutePath());
    }

    private static void writeRow(BufferedWriter writer, List<String> values) throws IOException {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) writer.write(',');
            writer.write(escape(values.get(index)));
        }
        writer.write("\r\n");
    }

    private static String escape(String value) {
        String cell = value == null ? "" : value;
        String trimmed = cell.stripLeading();
        if (!trimmed.isEmpty() && "=+-@".indexOf(trimmed.charAt(0)) >= 0) cell = "'" + cell;
        return "\"" + cell.replace("\"", "\"\"") + "\"";
    }
}
