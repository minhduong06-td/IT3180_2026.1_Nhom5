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

    @Test
    void demoDataContainsAResidentAccountButNoAuthorityAccount() throws IOException {
        String sql = Files.readString(DEMO_DATA);

        assertTrue(sql.contains("'nguyen.minh.an'"), "Thiếu tài khoản người dùng mẫu");
        assertTrue(sql.contains("'RESIDENT'"), "Thiếu role RESIDENT");
        assertTrue(sql.contains("apartment_id"), "Tài khoản người dùng chưa được gắn phạm vi căn hộ");
        org.junit.jupiter.api.Assertions.assertFalse(sql.contains("'coquan'"), "Không được seed tài khoản coquan");
        org.junit.jupiter.api.Assertions.assertFalse(sql.contains("AUTHORITY"), "Không được seed role Authority trong bản demo này");
    }

    @Test
    void demoAccountsContainLoginContactsForEachRole() throws IOException {
        String sql = Files.readString(DEMO_DATA);

        assertTrue(sql.contains("email"), "Thiếu cột email cho tài khoản demo");
        assertTrue(sql.contains("phone"), "Thiếu cột số điện thoại cho tài khoản demo");
        assertTrue(sql.contains("admin@bluemoon.vn"), "Thiếu email tài khoản quản trị");
        assertTrue(sql.contains("ngan@bluemoon.vn"), "Thiếu email tài khoản kế toán");
        assertTrue(sql.contains("minhan@gmail.com"), "Thiếu email tài khoản người dùng");
        org.junit.jupiter.api.Assertions.assertFalse(sql.contains("('accountant'"), "Không còn username accountant cũ");
        org.junit.jupiter.api.Assertions.assertFalse(sql.contains("('resident'"), "Không còn username resident cũ");
    }

    @Test
    void demoAccountUpsertRefreshesAStalePasswordHash() throws IOException {
        String sql = Files.readString(DEMO_DATA);

        assertTrue(sql.contains("password_hash = VALUES(password_hash)"),
                "Seed phải sửa lại hash cũ hoặc hỏng khi tài khoản demo đã tồn tại");
    }

    @Test
    void demoPaymentsDoNotAddSyntheticCreditOrConflictingHouseholdPayers() throws IOException {
        String sql = Files.readString(DEMO_DATA);

        assertTrue(sql.contains("'Nguyễn Minh An', c.amount, c.amount, 'BANK_TRANSFER',\n       'BM-SEED-A101-MANAGEMENT-1'"),
                "Các khoản thu của A101 phải ghi nhận theo chủ hộ thống nhất");
        assertTrue(!sql.contains("'Phạm Ngọc Mai', c.amount, c.amount, 'BANK_TRANSFER',\n       'BM-SEED-A101-MANAGEMENT-1'"),
                "Không tạo thêm người nộp khác cho cùng hộ nếu không cần thiết");
        assertTrue(!sql.contains("BM-SEED-C1002-CREDIT-1"),
                "Không seed khoản nộp dư chỉ để tạo trạng thái đa dạng");
        assertTrue(!sql.contains("INSERT INTO apartment_credits"),
                "Không seed số dư nộp trước nhân tạo");
    }
}
