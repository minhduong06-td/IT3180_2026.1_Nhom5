package com.bluemoon.dao;

import com.bluemoon.config.Database;
import com.bluemoon.model.Domain.*;
import com.bluemoon.service.BillingRules;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class AppRepository {
    private final Database database;

    public AppRepository(Database database) {
        this.database = database;
    }

    public LoginCredential findLoginCredential(String username) throws SQLException {
        String sql = """
                SELECT u.id, u.username, u.display_name, u.role, u.password_hash,
                       COALESCE(s.locked_until > CURRENT_TIMESTAMP, FALSE) AS login_locked
                FROM app_users u LEFT JOIN account_login_state s ON s.user_id = u.id
                WHERE u.username = ? AND u.active = TRUE
                """;
        try (Connection connection = database.openConnection(); PreparedStatement query = connection.prepareStatement(sql)) {
            query.setString(1, username);
            try (ResultSet result = query.executeQuery()) {
                if (!result.next()) return null;
                AppUser user = new AppUser(result.getLong("id"), result.getString("username"),
                        result.getString("display_name"), Role.valueOf(result.getString("role")));
                return new LoginCredential(user, result.getString("password_hash"), result.getBoolean("login_locked"));
            }
        }
    }

    public List<UserAccount> listUserAccounts() throws SQLException {
        String sql = """
                SELECT u.id, u.username, u.display_name, u.role, u.active,
                       COALESCE(s.locked_until > CURRENT_TIMESTAMP, FALSE) AS login_locked
                FROM app_users u LEFT JOIN account_login_state s ON s.user_id = u.id
                ORDER BY u.display_name, u.username
                """;
        List<UserAccount> users = new ArrayList<>();
        try (Connection connection = database.openConnection(); Statement query = connection.createStatement();
             ResultSet result = query.executeQuery(sql)) {
            while (result.next()) users.add(new UserAccount(result.getLong("id"), result.getString("username"),
                    result.getString("display_name"), Role.valueOf(result.getString("role")),
                    result.getBoolean("active"), result.getBoolean("login_locked")));
        }
        return users;
    }

    public void createUser(String username, String passwordHash, String displayName, Role role) throws SQLException {
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO app_users(username, password_hash, display_name, role) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, username);
                insert.setString(2, passwordHash);
                insert.setString(3, displayName);
                insert.setString(4, role.name());
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Không tạo được tài khoản.");
                    try (PreparedStatement state = connection.prepareStatement(
                            "INSERT INTO account_login_state(user_id) VALUES (?)")) {
                        state.setLong(1, keys.getLong(1));
                        state.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void updatePassword(long userId, String passwordHash) throws SQLException {
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement update = connection.prepareStatement(
                    "UPDATE app_users SET password_hash = ? WHERE id = ? AND active = TRUE")) {
                update.setString(1, passwordHash);
                update.setLong(2, userId);
                if (update.executeUpdate() != 1) throw new SQLException("Tài khoản không còn hoạt động.");
                try (PreparedStatement reset = connection.prepareStatement(
                        "UPDATE account_login_state SET failed_attempts = 0, locked_until = NULL WHERE user_id = ?")) {
                    reset.setLong(1, userId);
                    reset.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public boolean recordLoginFailure(long userId) throws SQLException {
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ensure = connection.prepareStatement(
                        "INSERT IGNORE INTO account_login_state(user_id) VALUES (?)")) {
                    ensure.setLong(1, userId);
                    ensure.executeUpdate();
                }
                int failures;
                boolean locked;
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT failed_attempts, COALESCE(locked_until > CURRENT_TIMESTAMP, FALSE) AS login_locked "
                                + "FROM account_login_state WHERE user_id = ? FOR UPDATE")) {
                    query.setLong(1, userId);
                    try (ResultSet result = query.executeQuery()) {
                        if (!result.next()) throw new SQLException("Không tải được trạng thái đăng nhập.");
                        failures = result.getInt("failed_attempts");
                        locked = result.getBoolean("login_locked");
                    }
                }
                if (locked) {
                    connection.commit();
                    return true;
                }
                boolean lockAccount = failures + 1 >= 5;
                String updateSql = lockAccount
                        ? "UPDATE account_login_state SET failed_attempts = 0, locked_until = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 5 MINUTE) WHERE user_id = ?"
                        : "UPDATE account_login_state SET failed_attempts = failed_attempts + 1 WHERE user_id = ?";
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setLong(1, userId);
                    update.executeUpdate();
                }
                connection.commit();
                return lockAccount;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void resetLoginFailures(long userId) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement update = connection.prepareStatement(
                     "UPDATE account_login_state SET failed_attempts = 0, locked_until = NULL WHERE user_id = ?")) {
            update.setLong(1, userId);
            update.executeUpdate();
        }
    }

    public String findPasswordHash(long userId) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT password_hash FROM app_users WHERE id = ? AND active = TRUE")) {
            query.setLong(1, userId);
            try (ResultSet result = query.executeQuery()) {
                return result.next() ? result.getString(1) : null;
            }
        }
    }

    public List<Apartment> listApartments() throws SQLException {
        String sql = "SELECT id, code, area_m2, household_name, contact_phone, active FROM apartments ORDER BY code";
        List<Apartment> apartments = new ArrayList<>();
        try (Connection connection = database.openConnection(); Statement query = connection.createStatement();
             ResultSet result = query.executeQuery(sql)) {
            while (result.next()) {
                apartments.add(new Apartment(result.getLong("id"), result.getString("code"),
                        result.getBigDecimal("area_m2"), result.getString("household_name"),
                        result.getString("contact_phone"), result.getBoolean("active")));
            }
        }
        return apartments;
    }

    public void saveApartment(Apartment apartment) throws SQLException {
        if (apartment.id() == null) {
            String sql = "INSERT INTO apartments(code, area_m2, household_name, contact_phone, active) VALUES (?, ?, ?, ?, ?)";
            try (Connection connection = database.openConnection(); PreparedStatement insert = connection.prepareStatement(sql)) {
                bindApartment(insert, apartment);
                insert.executeUpdate();
            }
            return;
        }
        String sql = "UPDATE apartments SET code = ?, area_m2 = ?, household_name = ?, contact_phone = ?, active = ? WHERE id = ?";
        try (Connection connection = database.openConnection(); PreparedStatement update = connection.prepareStatement(sql)) {
            bindApartment(update, apartment);
            update.setLong(6, apartment.id());
            if (update.executeUpdate() != 1) throw new SQLException("Không tìm thấy căn hộ cần cập nhật.");
        }
    }

    private static void bindApartment(PreparedStatement statement, Apartment apartment) throws SQLException {
        statement.setString(1, apartment.code());
        statement.setBigDecimal(2, apartment.areaM2());
        statement.setString(3, apartment.householdName());
        statement.setString(4, apartment.contactPhone());
        statement.setBoolean(5, apartment.active());
    }

    public List<FeeType> listFeeTypes() throws SQLException {
        String sql = """
                SELECT f.id, f.code, f.name, f.required, f.calculation_basis, f.active,
                       r.unit_price, r.effective_from
                FROM fee_types f
                LEFT JOIN fee_rates r ON r.id = (
                    SELECT r2.id FROM fee_rates r2 WHERE r2.fee_type_id = f.id
                    ORDER BY r2.effective_from DESC LIMIT 1)
                ORDER BY f.name
                """;
        List<FeeType> fees = new ArrayList<>();
        try (Connection connection = database.openConnection(); Statement query = connection.createStatement();
             ResultSet result = query.executeQuery(sql)) {
            while (result.next()) {
                Date effective = result.getDate("effective_from");
                fees.add(new FeeType(result.getLong("id"), result.getString("code"),
                        result.getString("name"), result.getBoolean("required"),
                        CalculationBasis.valueOf(result.getString("calculation_basis")),
                        result.getBigDecimal("unit_price"), effective == null ? null : effective.toLocalDate(),
                        result.getBoolean("active")));
            }
        }
        return fees;
    }

    public void createFeeType(String code, String name, boolean required, CalculationBasis basis,
                              BigDecimal unitPrice, LocalDate effectiveFrom, long userId) throws SQLException {
        String sql = "INSERT INTO fee_types(code, name, required, calculation_basis) VALUES (?, ?, ?, ?)";
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement insert = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, code);
                insert.setString(2, name);
                insert.setBoolean(3, required);
                insert.setString(4, basis.name());
                insert.executeUpdate();
                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Không tạo được loại khoản thu.");
                    insertRate(connection, keys.getLong(1), unitPrice, effectiveFrom, userId);
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public void addFeeRate(long feeId, BigDecimal unitPrice, LocalDate effectiveFrom, long userId) throws SQLException {
        try (Connection connection = database.openConnection()) {
            insertRate(connection, feeId, unitPrice, effectiveFrom, userId);
        }
    }

    private static void insertRate(Connection connection, long feeId, BigDecimal unitPrice,
                                   LocalDate effectiveFrom, long userId) throws SQLException {
        String sql = "INSERT INTO fee_rates(fee_type_id, unit_price, effective_from, created_by) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insert = connection.prepareStatement(sql)) {
            insert.setLong(1, feeId);
            insert.setBigDecimal(2, unitPrice);
            insert.setDate(3, Date.valueOf(effectiveFrom));
            insert.setLong(4, userId);
            insert.executeUpdate();
        }
    }

    public List<BillingPeriod> listBillingPeriods() throws SQLException {
        String sql = """
                SELECT p.id, p.period_year, p.period_month, p.issue_date, p.due_date,
                       COUNT(c.id) AS charge_count, COALESCE(SUM(c.amount), 0) AS total_amount
                FROM billing_periods p LEFT JOIN charges c ON c.billing_period_id = p.id
                GROUP BY p.id ORDER BY p.period_year DESC, p.period_month DESC
                """;
        List<BillingPeriod> periods = new ArrayList<>();
        try (Connection connection = database.openConnection(); Statement query = connection.createStatement();
             ResultSet result = query.executeQuery(sql)) {
            while (result.next()) periods.add(readPeriod(result));
        }
        return periods;
    }

    public BillingPeriod issuePeriod(int year, int month, LocalDate issueDate,
                                     LocalDate dueDate, long userId) throws SQLException {
        String addPeriod = "INSERT INTO billing_periods(period_year, period_month, issue_date, due_date, created_by) VALUES (?, ?, ?, ?, ?)";
        String loadInputs = """
                SELECT a.id AS apartment_id, a.area_m2, f.id AS fee_id, f.calculation_basis,
                       r.unit_price
                FROM apartments a CROSS JOIN fee_types f
                JOIN fee_rates r ON r.fee_type_id = f.id
                  AND r.effective_from = (SELECT MAX(r2.effective_from) FROM fee_rates r2
                                          WHERE r2.fee_type_id = f.id AND r2.effective_from <= ?)
                WHERE a.active = TRUE AND f.active = TRUE AND f.required = TRUE
                ORDER BY a.code, f.name
                """;
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                long periodId;
                try (PreparedStatement insert = connection.prepareStatement(addPeriod, Statement.RETURN_GENERATED_KEYS)) {
                    insert.setInt(1, year);
                    insert.setInt(2, month);
                    insert.setDate(3, Date.valueOf(issueDate));
                    insert.setDate(4, Date.valueOf(dueDate));
                    insert.setLong(5, userId);
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không tạo được kỳ thu.");
                        periodId = keys.getLong(1);
                    }
                }

                int apartmentCount = countActive(connection, "apartments");
                int feeCount = countRequiredFees(connection);
                int expected = apartmentCount * feeCount;
                if (expected == 0) throw new SQLException("Cần có ít nhất một căn hộ đang hoạt động và một khoản thu bắt buộc.");

                List<ChargeInput> inputs = new ArrayList<>();
                try (PreparedStatement query = connection.prepareStatement(loadInputs)) {
                    query.setDate(1, Date.valueOf(LocalDate.of(year, month, 1)));
                    try (ResultSet result = query.executeQuery()) {
                        while (result.next()) {
                            inputs.add(new ChargeInput(result.getLong("apartment_id"), result.getBigDecimal("area_m2"),
                                    result.getLong("fee_id"), CalculationBasis.valueOf(result.getString("calculation_basis")),
                                    result.getBigDecimal("unit_price")));
                        }
                    }
                }
                if (inputs.size() != expected) throw new SQLException("Thiếu đơn giá có hiệu lực cho một hoặc nhiều khoản thu.");

                String addCharge = """
                        INSERT INTO charges(billing_period_id, apartment_id, fee_type_id, calculation_basis_snapshot,
                                            quantity_snapshot, unit_price_snapshot, amount)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """;
                BigDecimal total = BigDecimal.ZERO;
                try (PreparedStatement insert = connection.prepareStatement(addCharge)) {
                    for (ChargeInput input : inputs) {
                        BigDecimal quantity = input.basis() == CalculationBasis.PER_M2 ? input.areaM2() : BigDecimal.ONE;
                        BigDecimal amount = BillingRules.calculate(input.basis(), input.areaM2(), input.unitPrice());
                        insert.setLong(1, periodId);
                        insert.setLong(2, input.apartmentId());
                        insert.setLong(3, input.feeId());
                        insert.setString(4, input.basis().name());
                        insert.setBigDecimal(5, quantity);
                        insert.setBigDecimal(6, input.unitPrice());
                        insert.setBigDecimal(7, amount);
                        insert.addBatch();
                        total = total.add(amount);
                    }
                    insert.executeBatch();
                }
                connection.commit();
                return new BillingPeriod(periodId, year, month, issueDate, dueDate, inputs.size(), total);
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static int countActive(Connection connection, String table) throws SQLException {
        String sql = "apartments".equals(table) ? "SELECT COUNT(*) FROM apartments WHERE active = TRUE"
                : "SELECT COUNT(*) FROM fee_types WHERE active = TRUE AND required = TRUE";
        try (Statement query = connection.createStatement(); ResultSet result = query.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }

    private static int countRequiredFees(Connection connection) throws SQLException {
        try (Statement query = connection.createStatement();
             ResultSet result = query.executeQuery("SELECT COUNT(*) FROM fee_types WHERE active = TRUE AND required = TRUE")) {
            result.next();
            return result.getInt(1);
        }
    }

    public List<ChargeSummary> listCharges(Long periodId, String search) throws SQLException {
        String sql = """
                SELECT c.id, c.billing_period_id, a.code, a.household_name, f.name AS fee_name,
                       CONCAT(LPAD(p.period_month, 2, '0'), '/', p.period_year) AS period_label,
                       c.calculation_basis_snapshot, c.quantity_snapshot, c.unit_price_snapshot,
                       c.amount, p.due_date,
                       (SELECT COALESCE(SUM(pm.amount), 0) FROM payments pm WHERE pm.charge_id = c.id) AS paid
                FROM charges c
                JOIN apartments a ON a.id = c.apartment_id
                JOIN fee_types f ON f.id = c.fee_type_id
                JOIN billing_periods p ON p.id = c.billing_period_id
                WHERE (? IS NULL OR c.billing_period_id = ?)
                  AND (? = '' OR a.code LIKE ? OR a.household_name LIKE ? OR f.name LIKE ?)
                ORDER BY p.period_year DESC, p.period_month DESC, a.code, f.name
                """;
        List<ChargeSummary> charges = new ArrayList<>();
        String pattern = "%" + (search == null ? "" : search.trim()) + "%";
        try (Connection connection = database.openConnection(); PreparedStatement query = connection.prepareStatement(sql)) {
            if (periodId == null) {
                query.setNull(1, java.sql.Types.BIGINT);
                query.setNull(2, java.sql.Types.BIGINT);
            } else {
                query.setLong(1, periodId);
                query.setLong(2, periodId);
            }
            query.setString(3, search == null ? "" : search.trim());
            query.setString(4, pattern);
            query.setString(5, pattern);
            query.setString(6, pattern);
            try (ResultSet result = query.executeQuery()) {
                while (result.next()) {
                    BigDecimal amount = result.getBigDecimal("amount");
                    BigDecimal paid = result.getBigDecimal("paid");
                    charges.add(new ChargeSummary(result.getLong("id"), result.getLong("billing_period_id"), result.getString("code"),
                            result.getString("household_name"), result.getString("fee_name"),
                            result.getString("period_label"), CalculationBasis.valueOf(result.getString("calculation_basis_snapshot")),
                            result.getBigDecimal("quantity_snapshot"), result.getBigDecimal("unit_price_snapshot"),
                            amount, paid, amount.subtract(paid), BillingRules.status(amount, paid),
                            result.getDate("due_date").toLocalDate()));
                }
            }
        }
        return charges;
    }

    public void recordPayment(long chargeId, BigDecimal amount, PaymentMethod method,
                              String reference, String note, long userId) throws SQLException {
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal chargeAmount;
                try (PreparedStatement lock = connection.prepareStatement("SELECT amount FROM charges WHERE id = ? FOR UPDATE")) {
                    lock.setLong(1, chargeId);
                    try (ResultSet result = lock.executeQuery()) {
                        if (!result.next()) throw new SQLException("Không tìm thấy khoản phải thu.");
                        chargeAmount = result.getBigDecimal(1);
                    }
                }
                BigDecimal paid;
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE charge_id = ?")) {
                    query.setLong(1, chargeId);
                    try (ResultSet result = query.executeQuery()) {
                        result.next();
                        paid = result.getBigDecimal(1);
                    }
                }
                BillingRules.validatePayment(amount, chargeAmount.subtract(paid));
                String sql = "INSERT INTO payments(charge_id, amount, method, reference, note, recorded_by) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement insert = connection.prepareStatement(sql)) {
                    insert.setLong(1, chargeId);
                    insert.setBigDecimal(2, amount);
                    insert.setString(3, method.name());
                    insert.setString(4, blankToNull(reference));
                    insert.setString(5, blankToNull(note));
                    insert.setLong(6, userId);
                    insert.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public List<PaymentRecord> listPayments(String search) throws SQLException {
        String sql = """
                SELECT pm.id, a.code, a.household_name, f.name AS fee_name,
                       CONCAT(LPAD(p.period_month, 2, '0'), '/', p.period_year) AS period_label,
                       pm.paid_at, pm.amount, pm.method, pm.reference, pm.note, u.display_name
                FROM payments pm
                JOIN charges c ON c.id = pm.charge_id
                JOIN apartments a ON a.id = c.apartment_id
                JOIN fee_types f ON f.id = c.fee_type_id
                JOIN billing_periods p ON p.id = c.billing_period_id
                JOIN app_users u ON u.id = pm.recorded_by
                WHERE (? = '' OR a.code LIKE ? OR a.household_name LIKE ? OR f.name LIKE ? OR pm.reference LIKE ? OR pm.note LIKE ?)
                ORDER BY pm.paid_at DESC, pm.id DESC
                """;
        List<PaymentRecord> payments = new ArrayList<>();
        String term = search == null ? "" : search.trim();
        String pattern = "%" + term + "%";
        try (Connection connection = database.openConnection(); PreparedStatement query = connection.prepareStatement(sql)) {
            query.setString(1, term);
            query.setString(2, pattern);
            query.setString(3, pattern);
            query.setString(4, pattern);
            query.setString(5, pattern);
            query.setString(6, pattern);
            try (ResultSet result = query.executeQuery()) {
                while (result.next()) {
                    Timestamp paidAt = result.getTimestamp("paid_at");
                    payments.add(new PaymentRecord(result.getLong("id"), result.getString("code"),
                            result.getString("household_name"), result.getString("fee_name"),
                            result.getString("period_label"), paidAt.toLocalDateTime(),
                            result.getBigDecimal("amount"), PaymentMethod.valueOf(result.getString("method")),
                            result.getString("reference"), result.getString("note"), result.getString("display_name")));
                }
            }
        }
        return payments;
    }

    public DashboardSummary dashboard() throws SQLException {
        List<BillingPeriod> periods = listBillingPeriods();
        if (periods.isEmpty()) return new DashboardSummary("Chưa phát hành", BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, 0);
        BillingPeriod current = periods.getFirst();
        List<ChargeSummary> charges = listCharges(current.id(), "");
        BigDecimal total = charges.stream().map(ChargeSummary::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = charges.stream().map(ChargeSummary::paid).reduce(BigDecimal.ZERO, BigDecimal::add);
        long paidCount = charges.stream().filter(c -> c.status() == ChargeStatus.PAID).count();
        long openCount = charges.size() - paidCount;
        int apartments = (int) charges.stream().map(ChargeSummary::apartmentCode).distinct().count();
        return new DashboardSummary(current.label(), total, paid, total.subtract(paid), apartments,
                Math.toIntExact(paidCount), Math.toIntExact(openCount));
    }

    private static BillingPeriod readPeriod(ResultSet result) throws SQLException {
        Date issue = result.getDate("issue_date");
        Date due = result.getDate("due_date");
        return new BillingPeriod(result.getLong("id"), result.getInt("period_year"), result.getInt("period_month"),
                issue.toLocalDate(), due.toLocalDate(), result.getInt("charge_count"), result.getBigDecimal("total_amount"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record ChargeInput(long apartmentId, BigDecimal areaM2, long feeId,
                               CalculationBasis basis, BigDecimal unitPrice) {}
}
