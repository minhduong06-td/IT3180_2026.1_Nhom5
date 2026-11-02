package com.bluemoon.config;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseBootstrap {
    private final Database database;

    public DatabaseBootstrap(Database database) {
        this.database = database;
    }

    public void initialize() throws SQLException, IOException {
        String schema;
        try (InputStream input = getClass().getResourceAsStream("/db/schema.sql")) {
            if (input == null) throw new IOException("Không tìm thấy cấu trúc cơ sở dữ liệu.");
            schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        try (Connection connection = database.openConnection(); Statement statement = connection.createStatement()) {
            for (String sql : schema.split(";")) {
                if (!sql.isBlank()) statement.execute(sql);
            }
            ensureApartmentProfile(connection);
            ensureFeeCalculationBasis(connection);
            ensurePaymentProfile(connection);
            seedInitialData(connection);
        }
    }

    private static void ensurePaymentProfile(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            if (!hasColumn(connection, "payments", "payer_name")) {
                statement.executeUpdate("ALTER TABLE payments ADD COLUMN payer_name VARCHAR(50) NULL");
            }
            if (!hasColumn(connection, "payments", "applied_amount")) {
                statement.executeUpdate("ALTER TABLE payments ADD COLUMN applied_amount DECIMAL(15, 0) NULL");
            }
            if (isNullable(connection, "payments", "payer_name")) {
                statement.executeUpdate("UPDATE payments SET payer_name = 'Chưa ghi nhận' WHERE payer_name IS NULL OR payer_name = ''");
                statement.executeUpdate("ALTER TABLE payments MODIFY payer_name VARCHAR(50) NOT NULL");
            }
            if (isNullable(connection, "payments", "applied_amount")) {
                statement.executeUpdate("UPDATE payments SET applied_amount = amount WHERE applied_amount IS NULL");
                statement.executeUpdate("ALTER TABLE payments MODIFY applied_amount DECIMAL(15, 0) NOT NULL");
            }
        }
    }

    private static void ensureFeeCalculationBasis(Connection connection) throws SQLException {
        String feeType = columnType(connection, "fee_types", "calculation_basis");
        if (!feeType.contains("PER_MOTORCYCLE") || !feeType.contains("PER_CAR")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE fee_types MODIFY calculation_basis "
                        + "ENUM('PER_M2', 'PER_RESIDENT', 'PER_MOTORCYCLE', 'PER_CAR', 'FIXED', 'VOLUNTARY') NOT NULL");
            }
        }
        String chargeBasis = columnType(connection, "charges", "calculation_basis_snapshot");
        if (!chargeBasis.contains("PER_MOTORCYCLE") || !chargeBasis.contains("PER_CAR")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE charges MODIFY calculation_basis_snapshot "
                        + "ENUM('PER_M2', 'PER_RESIDENT', 'PER_MOTORCYCLE', 'PER_CAR', 'FIXED') NOT NULL");
            }
        }
    }

    private static String columnType(Connection connection, String table, String column) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT column_type FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?")) {
            query.setString(1, table);
            query.setString(2, column);
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) throw new SQLException("Không tìm thấy cột " + table + "." + column + ".");
                return result.getString(1);
            }
        }
    }

    private static void ensureApartmentProfile(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            if (!hasColumn(connection, "apartments", "household_code")) {
                statement.executeUpdate("ALTER TABLE apartments ADD COLUMN household_code VARCHAR(20) NULL");
            }
            if (!hasColumn(connection, "apartments", "move_in_date")) {
                statement.executeUpdate("ALTER TABLE apartments ADD COLUMN move_in_date DATE NULL");
            }
            if (!hasColumn(connection, "apartments", "occupancy_status")) {
                statement.executeUpdate("ALTER TABLE apartments ADD COLUMN occupancy_status "
                        + "ENUM('OCCUPIED', 'VACANT', 'RENTED') NOT NULL DEFAULT 'OCCUPIED'");
            }
            statement.executeUpdate("UPDATE apartments SET household_code = CONCAT('HK-', LPAD(id, 6, '0')) "
                    + "WHERE household_code IS NULL OR household_code = ''");
            statement.executeUpdate("UPDATE apartments SET move_in_date = DATE(created_at) WHERE move_in_date IS NULL");
            if (isNullable(connection, "apartments", "household_code")) {
                statement.executeUpdate("ALTER TABLE apartments MODIFY household_code VARCHAR(20) NOT NULL");
            }
            if (isNullable(connection, "apartments", "move_in_date")) {
                statement.executeUpdate("ALTER TABLE apartments MODIFY move_in_date DATE NOT NULL");
            }
            if (!hasIndex(connection, "apartments", "uq_apartment_household_code")) {
                statement.executeUpdate("ALTER TABLE apartments ADD UNIQUE KEY uq_apartment_household_code (household_code)");
            }
        }
    }

    private static boolean hasColumn(Connection connection, String table, String column) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?")) {
            query.setString(1, table);
            query.setString(2, column);
            try (ResultSet result = query.executeQuery()) {
                return result.next();
            }
        }
    }

    private static boolean isNullable(Connection connection, String table, String column) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT is_nullable FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?")) {
            query.setString(1, table);
            query.setString(2, column);
            try (ResultSet result = query.executeQuery()) {
                return result.next() && "YES".equals(result.getString(1));
            }
        }
    }

    private static boolean hasIndex(Connection connection, String table, String index) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?")) {
            query.setString(1, table);
            query.setString(2, index);
            try (ResultSet result = query.executeQuery()) {
                return result.next();
            }
        }
    }

    private static void seedInitialData(Connection connection) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            seedUsers(connection);
            seedLoginState(connection);
            seedApartments(connection);
            seedFees(connection);
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private static void seedUsers(Connection connection) throws SQLException {
        if (count(connection, "app_users") != 0) return;
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO app_users(username, password_hash, display_name, role) VALUES (?, ?, ?, ?)")) {
            addUser(insert, "admin", "admin123", "Quản trị BlueMoon", "ADMIN");
            addUser(insert, "accountant", "accountant123", "Kế toán BlueMoon", "ACCOUNTANT");
        }
    }

    private static void seedLoginState(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT IGNORE INTO account_login_state(user_id) SELECT id FROM app_users");
        }
    }

    private static void addUser(PreparedStatement insert, String username, String password,
                                String displayName, String role) throws SQLException {
        insert.setString(1, username);
        insert.setString(2, BCrypt.hashpw(password, BCrypt.gensalt(10)));
        insert.setString(3, displayName);
        insert.setString(4, role);
        insert.executeUpdate();
    }

    private static void seedApartments(Connection connection) throws SQLException {
        if (count(connection, "apartments") != 0) return;
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO apartments(code, household_code, area_m2, household_name, contact_phone, move_in_date, occupancy_status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            addApartment(insert, "A101", "HK-000101", "65.00", "Nguyễn Minh An", "0900000101");
            addApartment(insert, "A102", "HK-000102", "78.50", "Trần Gia Bảo", "0900000102");
            addApartment(insert, "A103", "HK-000103", "92.00", "Lê Thu Hà", "0900000103");
        }
    }

    private static void addApartment(PreparedStatement insert, String code, String householdCode, String area,
                                     String household, String phone) throws SQLException {
        insert.setString(1, code);
        insert.setString(2, householdCode);
        insert.setBigDecimal(3, new java.math.BigDecimal(area));
        insert.setString(4, household);
        insert.setString(5, phone);
        insert.setDate(6, java.sql.Date.valueOf("2020-01-01"));
        insert.setString(7, "OCCUPIED");
        insert.executeUpdate();
    }

    private static void seedFees(Connection connection) throws SQLException {
        boolean missingDefaults = !hasFeeCode(connection, "SERVICE") || !hasFeeCode(connection, "MANAGEMENT")
                || !hasFeeCode(connection, "SANITATION") || !hasFeeCode(connection, "MOTORCYCLE_PARKING")
                || !hasFeeCode(connection, "CAR_PARKING");
        if (!missingDefaults) return;
        long adminId = findAdminId(connection);
        if (!hasFeeCode(connection, "SERVICE")) addFee(connection, "SERVICE", "Phí dịch vụ", "10000", "PER_M2", adminId);
        if (!hasFeeCode(connection, "MANAGEMENT")) addFee(connection, "MANAGEMENT", "Phí quản lý", "7000", "PER_M2", adminId);
        if (!hasFeeCode(connection, "SANITATION")) addFee(connection, "SANITATION", "Phí vệ sinh", "6000", "PER_RESIDENT", adminId);
        if (!hasFeeCode(connection, "MOTORCYCLE_PARKING")) addFee(connection, "MOTORCYCLE_PARKING", "Phí gửi xe máy", "70000", "PER_MOTORCYCLE", adminId);
        if (!hasFeeCode(connection, "CAR_PARKING")) addFee(connection, "CAR_PARKING", "Phí gửi ô tô", "1200000", "PER_CAR", adminId);
    }

    private static boolean hasFeeCode(Connection connection, String code) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement("SELECT 1 FROM fee_types WHERE code = ?")) {
            query.setString(1, code);
            try (ResultSet result = query.executeQuery()) {
                return result.next();
            }
        }
    }

    private static long addFee(Connection connection, String code, String name,
                               String unitPrice, String basis, long adminId) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO fee_types(code, name, required, calculation_basis) VALUES (?, ?, TRUE, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, code);
            insert.setString(2, name);
            insert.setString(3, basis);
            insert.executeUpdate();
            long id;
            try (ResultSet keys = insert.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Không tạo được khoản thu.");
                id = keys.getLong(1);
            }
            addRate(connection, id, unitPrice, adminId);
            return id;
        }
    }

    private static void addRate(Connection connection, long feeId, String unitPrice,
                                long adminId) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO fee_rates(fee_type_id, unit_price, effective_from, created_by) VALUES (?, ?, '2026-01-01', ?)")) {
            insert.setLong(1, feeId);
            insert.setBigDecimal(2, new java.math.BigDecimal(unitPrice));
            insert.setLong(3, adminId);
            insert.executeUpdate();
        }
    }

    private static long findAdminId(Connection connection) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT id FROM app_users WHERE role = 'ADMIN' ORDER BY id LIMIT 1");
             ResultSet result = query.executeQuery()) {
            if (!result.next()) throw new SQLException("Cần có tài khoản quản trị để khởi tạo đơn giá.");
            return result.getLong(1);
        }
    }

    private static int count(Connection connection, String table) throws SQLException {
        if (!table.matches("[a-z_]+")) throw new IllegalArgumentException("Tên bảng không hợp lệ.");
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            result.next();
            return result.getInt(1);
        }
    }
}
