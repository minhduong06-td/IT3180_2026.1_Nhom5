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
            seedInitialData(connection);
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
                "INSERT INTO apartments(code, area_m2, household_name, contact_phone) VALUES (?, ?, ?, ?)")) {
            addApartment(insert, "A101", "65.00", "Nguyễn Minh An", "0900000101");
            addApartment(insert, "A102", "78.50", "Trần Gia Bảo", "0900000102");
            addApartment(insert, "A103", "92.00", "Lê Thu Hà", "0900000103");
        }
    }

    private static void addApartment(PreparedStatement insert, String code, String area,
                                     String household, String phone) throws SQLException {
        insert.setString(1, code);
        insert.setBigDecimal(2, new java.math.BigDecimal(area));
        insert.setString(3, household);
        insert.setString(4, phone);
        insert.executeUpdate();
    }

    private static void seedFees(Connection connection) throws SQLException {
        if (count(connection, "fee_types") != 0) return;
        long adminId = findAdminId(connection);
        addFee(connection, "SERVICE", "Phí dịch vụ", "3000", adminId);
        addFee(connection, "MANAGEMENT", "Phí quản lý", "2000", adminId);
    }

    private static long addFee(Connection connection, String code, String name,
                               String unitPrice, long adminId) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT INTO fee_types(code, name, required, calculation_basis) VALUES (?, ?, TRUE, 'PER_M2')",
                Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, code);
            insert.setString(2, name);
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
