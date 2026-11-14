package com.bluemoon.config;

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
            ensureUserProfile(connection);
            ensureFeeCalculationBasis(connection);
            ensureFeeProfile(connection);
            ensurePaymentProfile(connection);
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
            if (!hasConstraint(connection, "payments", "chk_payment_applied")) {
                statement.executeUpdate("ALTER TABLE payments ADD CONSTRAINT chk_payment_applied "
                        + "CHECK (applied_amount >= 0 AND applied_amount <= amount)");
            }
        }
    }

    private static void ensureFeeCalculationBasis(Connection connection) throws SQLException {
        String feeType = columnType(connection, "fee_types", "calculation_basis");
        if (!feeType.contains("PER_MOTORCYCLE") || !feeType.contains("PER_CAR")
                || !feeType.contains("EXTERNAL_BILL")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE fee_types MODIFY calculation_basis "
                        + "ENUM('PER_M2', 'PER_RESIDENT', 'PER_MOTORCYCLE', 'PER_CAR', 'FIXED', 'EXTERNAL_BILL', 'VOLUNTARY') NOT NULL");
            }
        }
        String chargeBasis = columnType(connection, "charges", "calculation_basis_snapshot");
        if (!chargeBasis.contains("PER_MOTORCYCLE") || !chargeBasis.contains("PER_CAR")
                || !chargeBasis.contains("EXTERNAL_BILL")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE charges MODIFY calculation_basis_snapshot "
                        + "ENUM('PER_M2', 'PER_RESIDENT', 'PER_MOTORCYCLE', 'PER_CAR', 'FIXED', 'EXTERNAL_BILL') NOT NULL");
            }
        }
    }

    private static void ensureFeeProfile(Connection connection) throws SQLException {
        if (!hasColumn(connection, "fee_types", "active")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE fee_types ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE");
            }
        }
    }

    private static void ensureUserProfile(Connection connection) throws SQLException {
        String roleType = columnType(connection, "app_users", "role");
        if (!roleType.contains("RESIDENT")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE app_users MODIFY role "
                        + "ENUM('ADMIN', 'ACCOUNTANT', 'RESIDENT') NOT NULL");
            }
        }
        if (!hasColumn(connection, "app_users", "apartment_id")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE app_users ADD COLUMN apartment_id BIGINT NULL AFTER role");
            }
        }
        if (!hasColumn(connection, "app_users", "email")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE app_users ADD COLUMN email VARCHAR(254) NULL AFTER display_name");
            }
        }
        if (!hasColumn(connection, "app_users", "phone")) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE app_users ADD COLUMN phone VARCHAR(20) NULL AFTER email");
            }
        }
        try (Statement statement = connection.createStatement()) {
            if (!hasIndex(connection, "app_users", "uq_app_users_email")) {
                statement.executeUpdate("ALTER TABLE app_users ADD UNIQUE KEY uq_app_users_email (email)");
            }
            if (!hasIndex(connection, "app_users", "uq_app_users_phone")) {
                statement.executeUpdate("ALTER TABLE app_users ADD UNIQUE KEY uq_app_users_phone (phone)");
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

    private static boolean hasConstraint(Connection connection, String table, String constraint) throws SQLException {
        try (PreparedStatement query = connection.prepareStatement(
                "SELECT 1 FROM information_schema.table_constraints "
                        + "WHERE constraint_schema = DATABASE() AND table_name = ? "
                        + "AND constraint_name = ? AND constraint_type = 'CHECK'")) {
            query.setString(1, table);
            query.setString(2, constraint);
            try (ResultSet result = query.executeQuery()) {
                return result.next();
            }
        }
    }
}
