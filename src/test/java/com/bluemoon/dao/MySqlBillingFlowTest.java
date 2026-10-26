package com.bluemoon.dao;

import com.bluemoon.config.Database;
import com.bluemoon.config.DatabaseBootstrap;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.ChargeSummary;
import com.bluemoon.model.Domain.PaymentMethod;
import com.bluemoon.model.Domain.Role;
import com.bluemoon.service.AccountLockedException;
import com.bluemoon.service.AccountService;
import com.bluemoon.service.AuthService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MySqlBillingFlowTest {
    @Test
    void issuesSnapshotChargesAndRecordsPartialPayment() throws Exception {
        String url = System.getenv("BLUEMOON_IT_DB_URL");
        Assumptions.assumeTrue(url != null && !url.isBlank(), "Set BLUEMOON_IT_DB_URL to run MySQL integration checks.");
        Database database = new Database(url, System.getenv("BLUEMOON_IT_DB_USER"), System.getenv("BLUEMOON_IT_DB_PASSWORD"));
        new DatabaseBootstrap(database).initialize();
        AppRepository repository = new AppRepository(database);
        long adminId = userId(database, "admin");
        long accountantId = userId(database, "accountant");
        long serviceFeeId = feeId(database, "SERVICE");
        LocalDate laterRateDate = LocalDate.of(2098, 1, 15);
        removePeriod(database, 2098, 1);
        removeRate(database, serviceFeeId, laterRateDate);
        addRate(database, serviceFeeId, new BigDecimal("9999"), laterRateDate, adminId);

        try {
            AuthService auth = new AuthService(repository);
            AppUser admin = auth.login("admin", "admin123");
            assertEquals("ADMIN", admin.role().name());
            assertNull(auth.login("admin", "wrong-password"));
            verifyAccountManagement(database, repository, auth, admin);
            var period = repository.issuePeriod(2098, 1, LocalDate.of(2098, 1, 20),
                    LocalDate.of(2098, 1, 31), adminId);
            assertEquals(6, period.chargeCount());
            assertEquals(new BigDecimal("1177500"), period.totalAmount());
            assertThrows(SQLException.class, () -> repository.issuePeriod(2098, 1,
                    LocalDate.of(2098, 1, 1), LocalDate.of(2098, 1, 31), adminId));

            ChargeSummary serviceCharge = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> "Phí dịch vụ".equals(charge.feeName())).findFirst().orElseThrow();
            assertEquals(new BigDecimal("195000"), serviceCharge.amount());
            repository.recordPayment(serviceCharge.id(), new BigDecimal("100000"), PaymentMethod.CASH,
                    null, "Kiểm tra thu một phần", accountantId);
            ChargeSummary updated = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> charge.id() == serviceCharge.id()).findFirst().orElseThrow();
            assertEquals(new BigDecimal("95000"), updated.balance());
            assertEquals("PARTIALLY_PAID", updated.status().name());
            assertThrows(IllegalArgumentException.class, () -> repository.recordPayment(serviceCharge.id(),
                    new BigDecimal("95001"), PaymentMethod.CASH, null, null, accountantId));

            repository.recordPayment(serviceCharge.id(), new BigDecimal("95000"), PaymentMethod.BANK_TRANSFER,
                    "BM-2098-A101", null, accountantId);
            ChargeSummary paid = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> charge.id() == serviceCharge.id()).findFirst().orElseThrow();
            assertEquals(BigDecimal.ZERO, paid.balance());
            assertEquals("PAID", paid.status().name());
        } finally {
            removePeriod(database, 2098, 1);
            removeRate(database, serviceFeeId, laterRateDate);
        }
    }

    private static void verifyAccountManagement(Database database, AppRepository repository,
                                                AuthService auth, AppUser admin) throws Exception {
        String username = "account-check-" + System.currentTimeMillis();
        AccountService accounts = new AccountService(repository);
        try {
            assertThrows(IllegalArgumentException.class, () -> accounts.create(admin, username,
                    "Tài khoản kiểm tra", Role.ACCOUNTANT, "x".repeat(73), "x".repeat(73)));
            accounts.create(admin, username, "Tài khoản kiểm tra", Role.ACCOUNTANT,
                    "BlueMoon123", "BlueMoon123");
            AppUser account = auth.login(username, "BlueMoon123");
            assertNotNull(account);
            accounts.changePassword(account, "BlueMoon123", "NewMoon123", "NewMoon123");
            assertNotNull(auth.login(username, "NewMoon123"));
            for (int attempt = 0; attempt < 4; attempt++) assertNull(auth.login(username, "wrong-password"));
            assertThrows(AccountLockedException.class, () -> auth.login(username, "wrong-password"));
            assertThrows(AccountLockedException.class, () -> auth.login(username, "NewMoon123"));
        } finally {
            try (Connection connection = database.openConnection();
                 PreparedStatement delete = connection.prepareStatement("DELETE FROM app_users WHERE username = ?")) {
                delete.setString(1, username);
                delete.executeUpdate();
            }
        }
    }

    private static long feeId(Database database, String code) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement query = connection.prepareStatement("SELECT id FROM fee_types WHERE code = ?")) {
            query.setString(1, code);
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) throw new SQLException("Không tìm thấy khoản thu: " + code);
                return result.getLong(1);
            }
        }
    }

    private static void addRate(Database database, long feeId, BigDecimal unitPrice,
                                LocalDate effectiveFrom, long userId) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement insert = connection.prepareStatement(
                     "INSERT INTO fee_rates(fee_type_id, unit_price, effective_from, created_by) VALUES (?, ?, ?, ?)")) {
            insert.setLong(1, feeId);
            insert.setBigDecimal(2, unitPrice);
            insert.setDate(3, java.sql.Date.valueOf(effectiveFrom));
            insert.setLong(4, userId);
            insert.executeUpdate();
        }
    }

    private static void removeRate(Database database, long feeId, LocalDate effectiveFrom) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement delete = connection.prepareStatement(
                     "DELETE FROM fee_rates WHERE fee_type_id = ? AND effective_from = ?")) {
            delete.setLong(1, feeId);
            delete.setDate(2, java.sql.Date.valueOf(effectiveFrom));
            delete.executeUpdate();
        }
    }

    private static long userId(Database database, String username) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement query = connection.prepareStatement("SELECT id FROM app_users WHERE username = ?")) {
            query.setString(1, username);
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) throw new SQLException("Không tìm thấy tài khoản khởi tạo: " + username);
                return result.getLong(1);
            }
        }
    }

    private static void removePeriod(Database database, int year, int month) throws SQLException {
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                long id = -1;
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT id FROM billing_periods WHERE period_year = ? AND period_month = ?")) {
                    query.setInt(1, year);
                    query.setInt(2, month);
                    try (ResultSet result = query.executeQuery()) {
                        if (result.next()) id = result.getLong(1);
                    }
                }
                if (id >= 0) {
                    try (PreparedStatement deletePayments = connection.prepareStatement(
                            "DELETE FROM payments WHERE charge_id IN (SELECT id FROM charges WHERE billing_period_id = ?)")) {
                        deletePayments.setLong(1, id);
                        deletePayments.executeUpdate();
                    }
                    try (PreparedStatement deleteCharges = connection.prepareStatement(
                            "DELETE FROM charges WHERE billing_period_id = ?")) {
                        deleteCharges.setLong(1, id);
                        deleteCharges.executeUpdate();
                    }
                    try (PreparedStatement deletePeriod = connection.prepareStatement("DELETE FROM billing_periods WHERE id = ?")) {
                        deletePeriod.setLong(1, id);
                        deletePeriod.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
}
