package com.bluemoon.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvExporterTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void writesExcelCsvWithVietnameseTextAndSafeEscaping() throws IOException {
        Path file = temporaryDirectory.resolve("bao-cao.csv");
        CsvExporter.write(file, List.of("Người báo", "Mô tả", "Số tiền"), List.of(
                List.of("Nguyễn \"An\", căn hộ", "Vòi nước\nrò rỉ", "350000"),
                List.of("  =1+1", "Đã xử lý", "0")));

        String expected = "\uFEFFsep=,\r\n"
                + "\"Người báo\",\"Mô tả\",\"Số tiền\"\r\n"
                + "\"Nguyễn \"\"An\"\", căn hộ\",\"Vòi nước\nrò rỉ\",\"350000\"\r\n"
                + "\"'  =1+1\",\"Đã xử lý\",\"0\"\r\n";
        assertEquals(expected, Files.readString(file, StandardCharsets.UTF_8));
    }
}
