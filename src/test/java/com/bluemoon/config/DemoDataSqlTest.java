package com.bluemoon.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoDataSqlTest {
    private static final Path DEMO_DATA = Path.of("database", "demo-data.sql");

    @Test
    void demoDataContainsApartmentsAcrossSeveralFloors() throws IOException {
        String sql = Files.readString(DEMO_DATA);

        List.of("A101", "A204", "A305", "A407", "A508", "A612", "B703", "B809", "C1002", "C1205")
                .forEach(code -> assertTrue(sql.contains("'" + code + "'"), "Thiếu căn mẫu " + code));
        assertTrue(sql.contains("'OCCUPIED'"), "Thiếu căn đang ở");
        assertTrue(sql.contains("'RENTED'"), "Thiếu căn cho thuê");
        assertTrue(sql.contains("'VACANT'"), "Thiếu căn trống");
    }

    @Test
    void demoDataContainsVariedHouseholdRelationshipsAndOperationalRecords() throws IOException {
        String sql = Files.readString(DEMO_DATA);

        List.of("'HEAD'", "'WIFE'", "'HUSBAND'", "'CHILD'", "'PARENT'", "'TENANT'")
                .forEach(relationship -> assertTrue(sql.contains(relationship), "Thiếu quan hệ " + relationship));
        assertTrue(sql.contains("INSERT INTO vehicles"), "Thiếu dữ liệu phương tiện mẫu");
        assertTrue(sql.contains("INSERT INTO utility_bills"), "Thiếu dữ liệu tiện ích mẫu");
        assertTrue(sql.contains("INSERT INTO contributions"), "Thiếu dữ liệu đóng góp mẫu");
        assertTrue(sql.contains("INSERT INTO resident_requests"), "Thiếu dữ liệu yêu cầu cư dân mẫu");
    }
}
