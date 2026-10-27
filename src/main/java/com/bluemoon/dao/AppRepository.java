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
import java.sql.Types;
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
        String sql = "SELECT id, code, household_code, area_m2, household_name, contact_phone, "
                + "move_in_date, occupancy_status, active FROM apartments ORDER BY code";
        List<Apartment> apartments = new ArrayList<>();
        try (Connection connection = database.openConnection(); Statement query = connection.createStatement();
             ResultSet result = query.executeQuery(sql)) {
            while (result.next()) apartments.add(readApartment(result));
        }
        return apartments;
    }

    public void saveApartment(Apartment apartment) throws SQLException {
        if (apartment.id() == null) {
            String sql = "INSERT INTO apartments(code, household_code, area_m2, household_name, contact_phone, "
                    + "move_in_date, occupancy_status, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection connection = database.openConnection(); PreparedStatement insert = connection.prepareStatement(sql)) {
                bindApartment(insert, apartment);
                insert.executeUpdate();
            }
            return;
        }
        String sql = "UPDATE apartments SET code = ?, household_code = ?, area_m2 = ?, household_name = ?, "
                + "contact_phone = ?, move_in_date = ?, occupancy_status = ?, active = ? WHERE id = ?";
        try (Connection connection = database.openConnection(); PreparedStatement update = connection.prepareStatement(sql)) {
            bindApartment(update, apartment);
            update.setLong(9, apartment.id());
            if (update.executeUpdate() != 1) throw new SQLException("Không tìm thấy căn hộ cần cập nhật.");
        }
    }

    private static void bindApartment(PreparedStatement statement, Apartment apartment) throws SQLException {
        statement.setString(1, apartment.code());
        statement.setString(2, apartment.householdCode());
        statement.setBigDecimal(3, apartment.areaM2());
        statement.setString(4, apartment.householdName());
        statement.setString(5, apartment.contactPhone());
        statement.setDate(6, Date.valueOf(apartment.moveInDate()));
        statement.setString(7, apartment.occupancyStatus().name());
        statement.setBoolean(8, apartment.active());
    }

    private static Apartment readApartment(ResultSet result) throws SQLException {
        return new Apartment(result.getLong("id"), result.getString("code"), result.getString("household_code"),
                result.getBigDecimal("area_m2"), result.getString("household_name"), result.getString("contact_phone"),
                result.getDate("move_in_date").toLocalDate(), OccupancyStatus.valueOf(result.getString("occupancy_status")),
                result.getBoolean("active"));
    }

    public List<Resident> listResidents(long apartmentId) throws SQLException {
        String sql = "SELECT id, apartment_id, full_name, date_of_birth, gender, national_id, relationship, move_in_date "
                + "FROM residents WHERE apartment_id = ? ORDER BY relationship = 'HEAD' DESC, full_name";
        List<Resident> residents = new ArrayList<>();
        try (Connection connection = database.openConnection(); PreparedStatement query = connection.prepareStatement(sql)) {
            query.setLong(1, apartmentId);
            try (ResultSet result = query.executeQuery()) {
                while (result.next()) residents.add(readResident(result));
            }
        }
        return residents;
    }

    public List<ResidentDirectoryEntry> listResidentDirectory() throws SQLException {
        String sql = """
                SELECT a.id AS apartment_id, a.code, a.household_code, a.area_m2, a.household_name,
                       a.contact_phone, a.move_in_date AS apartment_move_in, a.occupancy_status, a.active,
                       r.id AS resident_id, r.full_name, r.date_of_birth, r.gender, r.national_id,
                       r.relationship, r.move_in_date AS resident_move_in
                FROM apartments a LEFT JOIN residents r ON r.apartment_id = a.id
                ORDER BY a.code, r.relationship = 'HEAD' DESC, r.full_name
                """;
        List<ResidentDirectoryEntry> entries = new ArrayList<>();
        try (Connection connection = database.openConnection(); Statement query = connection.createStatement();
             ResultSet result = query.executeQuery(sql)) {
            while (result.next()) {
                Apartment apartment = new Apartment(result.getLong("apartment_id"), result.getString("code"),
                        result.getString("household_code"), result.getBigDecimal("area_m2"),
                        result.getString("household_name"), result.getString("contact_phone"),
                        result.getDate("apartment_move_in").toLocalDate(),
                        OccupancyStatus.valueOf(result.getString("occupancy_status")), result.getBoolean("active"));
                long residentId = result.getLong("resident_id");
                Resident resident = result.wasNull() ? null : new Resident(residentId, apartment.id(),
                        result.getString("full_name"), result.getDate("date_of_birth").toLocalDate(),
                        Gender.valueOf(result.getString("gender")), result.getString("national_id"),
                        HouseholdRelationship.valueOf(result.getString("relationship")),
                        result.getDate("resident_move_in").toLocalDate());
                entries.add(new ResidentDirectoryEntry(apartment, resident));
            }
        }
        return entries;
    }

    public void saveResident(Resident resident, boolean replaceHead) throws SQLException {
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement lock = connection.prepareStatement("SELECT id FROM apartments WHERE id = ? FOR UPDATE")) {
                    lock.setLong(1, resident.apartmentId());
                    try (ResultSet result = lock.executeQuery()) {
                        if (!result.next()) throw new SQLException("Không tìm thấy căn hộ cần cập nhật.");
                    }
                }
                if (replaceHead && resident.relationship() == HouseholdRelationship.HEAD) {
                    try (PreparedStatement replace = connection.prepareStatement(
                            "UPDATE residents SET relationship = 'OTHER' WHERE apartment_id = ? "
                                    + "AND relationship = 'HEAD' AND id <> ?")) {
                        replace.setLong(1, resident.apartmentId());
                        replace.setLong(2, resident.id() == null ? -1 : resident.id());
                        replace.executeUpdate();
                    }
                }
                if (resident.id() == null) {
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO residents(apartment_id, full_name, date_of_birth, gender, national_id, relationship, move_in_date) "
                                    + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                        bindResident(insert, resident);
                        insert.executeUpdate();
                    }
                } else {
                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE residents SET full_name = ?, date_of_birth = ?, gender = ?, national_id = ?, "
                                    + "relationship = ?, move_in_date = ? WHERE id = ? AND apartment_id = ?")) {
                        update.setString(1, resident.fullName());
                        update.setDate(2, Date.valueOf(resident.dateOfBirth()));
                        update.setString(3, resident.gender().name());
                        setNullableString(update, 4, resident.nationalId());
                        update.setString(5, resident.relationship().name());
                        update.setDate(6, Date.valueOf(resident.moveInDate()));
                        update.setLong(7, resident.id());
                        update.setLong(8, resident.apartmentId());
                        if (update.executeUpdate() != 1) throw new SQLException("Không tìm thấy nhân khẩu cần cập nhật.");
                    }
                }
                if (resident.relationship() == HouseholdRelationship.HEAD) {
                    try (PreparedStatement updateHousehold = connection.prepareStatement(
                            "UPDATE apartments SET household_name = ? WHERE id = ?")) {
                        updateHousehold.setString(1, resident.fullName());
                        updateHousehold.setLong(2, resident.apartmentId());
                        updateHousehold.executeUpdate();
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    private static Resident readResident(ResultSet result) throws SQLException {
        Date birthDate = result.getDate("date_of_birth");
        Date moveInDate = result.getDate("move_in_date");
        return new Resident(result.getLong("id"), result.getLong("apartment_id"), result.getString("full_name"),
                birthDate.toLocalDate(), Gender.valueOf(result.getString("gender")), result.getString("national_id"),
                HouseholdRelationship.valueOf(result.getString("relationship")), moveInDate.toLocalDate());
    }

    private static void bindResident(PreparedStatement statement, Resident resident) throws SQLException {
        statement.setLong(1, resident.apartmentId());
        statement.setString(2, resident.fullName());
        statement.setDate(3, Date.valueOf(resident.dateOfBirth()));
        statement.setString(4, resident.gender().name());
        setNullableString(statement, 5, resident.nationalId());
        statement.setString(6, resident.relationship().name());
        statement.setDate(7, Date.valueOf(resident.moveInDate()));
    }

    private static void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null) statement.setNull(index, Types.VARCHAR);
        else statement.setString(index, value);
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
                    if (required) insertRate(connection, keys.getLong(1), unitPrice, effectiveFrom, userId);
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

    public void setFeeActive(long feeId, boolean active) throws SQLException {
        try (Connection connection = database.openConnection(); PreparedStatement update = connection.prepareStatement(
                "UPDATE fee_types SET active = ? WHERE id = ?")) {
            update.setBoolean(1, active);
            update.setLong(2, feeId);
            if (update.executeUpdate() != 1) throw new SQLException("Không tìm thấy khoản thu.");
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
                SELECT a.id AS apartment_id, a.area_m2, COALESCE(rc.resident_count, 0) AS resident_count,
                       f.id AS fee_id, f.calculation_basis, r.unit_price
                FROM apartments a CROSS JOIN fee_types f
                JOIN fee_rates r ON r.fee_type_id = f.id
                  AND r.effective_from = (SELECT MAX(r2.effective_from) FROM fee_rates r2
                                          WHERE r2.fee_type_id = f.id AND r2.effective_from <= ?)
                LEFT JOIN (SELECT apartment_id, COUNT(*) AS resident_count FROM residents GROUP BY apartment_id) rc
                  ON rc.apartment_id = a.id
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
                                    result.getInt("resident_count"), result.getLong("fee_id"),
                                    CalculationBasis.valueOf(result.getString("calculation_basis")),
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
                        BigDecimal quantity = switch (input.basis()) {
                            case PER_M2 -> input.areaM2();
                            case PER_RESIDENT -> BigDecimal.valueOf(input.residentCount());
                            case FIXED -> BigDecimal.ONE;
                            case VOLUNTARY -> throw new SQLException("Không thể lập phí bắt buộc từ khoản đóng góp.");
                        };
                        BigDecimal amount = BillingRules.calculate(input.basis(), input.areaM2(),
                                input.unitPrice(), input.residentCount());
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
                applyCredits(connection, periodId);
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

    private static void applyCredits(Connection connection, long periodId) throws SQLException {
        String paidSql = """
                SELECT COALESCE((SELECT SUM(applied_amount) FROM payments WHERE charge_id = ?), 0)
                     + COALESCE((SELECT SUM(amount) FROM credit_applications WHERE charge_id = ?), 0)
                """;
        try (PreparedStatement charges = connection.prepareStatement(
                "SELECT id, apartment_id, amount FROM charges WHERE billing_period_id = ? ORDER BY apartment_id, id FOR UPDATE")) {
            charges.setLong(1, periodId);
            try (ResultSet chargeRows = charges.executeQuery()) {
                while (chargeRows.next()) {
                    long chargeId = chargeRows.getLong("id");
                    long apartmentId = chargeRows.getLong("apartment_id");
                    BigDecimal balance = chargeRows.getBigDecimal("amount");
                    try (PreparedStatement paid = connection.prepareStatement(paidSql)) {
                        paid.setLong(1, chargeId);
                        paid.setLong(2, chargeId);
                        try (ResultSet result = paid.executeQuery()) {
                            result.next();
                            balance = balance.subtract(result.getBigDecimal(1));
                        }
                    }
                    try (PreparedStatement credits = connection.prepareStatement(
                            "SELECT id, remaining_amount FROM apartment_credits WHERE apartment_id = ? AND remaining_amount > 0 ORDER BY id FOR UPDATE")) {
                        credits.setLong(1, apartmentId);
                        try (ResultSet creditRows = credits.executeQuery()) {
                            while (balance.signum() > 0 && creditRows.next()) {
                                long creditId = creditRows.getLong("id");
                                BigDecimal applied = balance.min(creditRows.getBigDecimal("remaining_amount"));
                                try (PreparedStatement insert = connection.prepareStatement(
                                        "INSERT INTO credit_applications(credit_id, charge_id, amount) VALUES (?, ?, ?)")) {
                                    insert.setLong(1, creditId);
                                    insert.setLong(2, chargeId);
                                    insert.setBigDecimal(3, applied);
                                    insert.executeUpdate();
                                }
                                try (PreparedStatement update = connection.prepareStatement(
                                        "UPDATE apartment_credits SET remaining_amount = remaining_amount - ? WHERE id = ? AND remaining_amount >= ?")) {
                                    update.setBigDecimal(1, applied);
                                    update.setLong(2, creditId);
                                    update.setBigDecimal(3, applied);
                                    if (update.executeUpdate() != 1) throw new SQLException("Không cập nhật được số dư nộp trước.");
                                }
                                balance = balance.subtract(applied);
                            }
                        }
                    }
                }
            }
        }
    }

    public List<ChargeSummary> listCharges(Long periodId, String search) throws SQLException {
        String sql = """
                SELECT c.id, c.billing_period_id, a.code, a.household_name, f.name AS fee_name,
                       CONCAT(LPAD(p.period_month, 2, '0'), '/', p.period_year) AS period_label,
                       c.calculation_basis_snapshot, c.quantity_snapshot, c.unit_price_snapshot,
                       c.amount, p.due_date,
                       (SELECT COALESCE(SUM(pm.applied_amount), 0) FROM payments pm WHERE pm.charge_id = c.id)
                         + (SELECT COALESCE(SUM(ca.amount), 0) FROM credit_applications ca WHERE ca.charge_id = c.id) AS paid
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

    public List<HouseholdDebt> listHouseholdDebts(long periodId) throws SQLException {
        String sql = """
                WITH charge_activity AS (
                    SELECT charge_id, SUM(paid) AS paid, MAX(last_paid) AS last_paid
                    FROM (
                        SELECT charge_id, SUM(applied_amount) AS paid, MAX(paid_at) AS last_paid
                        FROM payments GROUP BY charge_id
                        UNION ALL
                        SELECT charge_id, SUM(amount) AS paid, MAX(applied_at) AS last_paid
                        FROM credit_applications GROUP BY charge_id
                    ) activity GROUP BY charge_id
                ), credit_balances AS (
                    SELECT apartment_id, SUM(remaining_amount) AS credit_balance
                    FROM apartment_credits GROUP BY apartment_id
                ), resident_counts AS (
                    SELECT apartment_id, COUNT(*) AS resident_count FROM residents GROUP BY apartment_id
                )
                SELECT a.id, MAX(a.code) AS code, MAX(a.household_name) AS household_name,
                       MAX(a.area_m2) AS area_m2,
                       MAX(CASE WHEN a.code REGEXP '^[A-Z][0-9]{3,4}$'
                                THEN CAST(SUBSTRING(a.code, 2, CHAR_LENGTH(a.code) - 3) AS UNSIGNED)
                                ELSE 0 END) AS floor,
                       MAX(COALESCE(r.resident_count, 0)) AS resident_count,
                       SUM(c.amount) AS total_due, SUM(COALESCE(p.paid, 0)) AS paid,
                       MAX(COALESCE(cr.credit_balance, 0)) AS credit_balance, MAX(p.last_paid) AS last_paid
                FROM charges c
                JOIN apartments a ON a.id = c.apartment_id
                LEFT JOIN charge_activity p ON p.charge_id = c.id
                LEFT JOIN credit_balances cr ON cr.apartment_id = a.id
                LEFT JOIN resident_counts r ON r.apartment_id = a.id
                WHERE c.billing_period_id = ?
                GROUP BY a.id ORDER BY code
                """;
        List<HouseholdDebt> debts = new ArrayList<>();
        try (Connection connection = database.openConnection(); PreparedStatement query = connection.prepareStatement(sql)) {
            query.setLong(1, periodId);
            try (ResultSet result = query.executeQuery()) {
                while (result.next()) {
                    BigDecimal totalDue = result.getBigDecimal("total_due");
                    BigDecimal paid = result.getBigDecimal("paid");
                    Timestamp lastPaid = result.getTimestamp("last_paid");
                    debts.add(new HouseholdDebt(result.getString("code"), result.getString("household_name"),
                            result.getInt("floor"), result.getBigDecimal("area_m2"), result.getInt("resident_count"),
                            totalDue, paid, totalDue.subtract(paid), result.getBigDecimal("credit_balance"),
                            BillingRules.status(totalDue, paid),
                            lastPaid == null ? null : lastPaid.toLocalDateTime()));
                }
            }
        }
        return debts;
    }

    public PaymentReceipt recordPayment(long chargeId, BigDecimal amount, String payerName, LocalDate paidOn,
                                        PaymentMethod method, String reference, String note, long userId) throws SQLException {
        try (Connection connection = database.openConnection()) {
            connection.setAutoCommit(false);
            try {
                long apartmentId;
                BigDecimal chargeAmount;
                try (PreparedStatement lock = connection.prepareStatement(
                        "SELECT apartment_id, amount FROM charges WHERE id = ? FOR UPDATE")) {
                    lock.setLong(1, chargeId);
                    try (ResultSet result = lock.executeQuery()) {
                        if (!result.next()) throw new SQLException("Kh\u00f4ng t\u00ecm th\u1ea5y kho\u1ea3n ph\u1ea3i thu.");
                        apartmentId = result.getLong("apartment_id");
                        chargeAmount = result.getBigDecimal("amount");
                    }
                }
                BigDecimal paid;
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT COALESCE((SELECT SUM(applied_amount) FROM payments WHERE charge_id = ?), 0)"
                                + " + COALESCE((SELECT SUM(amount) FROM credit_applications WHERE charge_id = ?), 0)")) {
                    query.setLong(1, chargeId);
                    query.setLong(2, chargeId);
                    try (ResultSet result = query.executeQuery()) {
                        result.next();
                        paid = result.getBigDecimal(1);
                    }
                }
                BigDecimal balance = chargeAmount.subtract(paid);
                BillingRules.validatePayment(amount, balance);
                BigDecimal applied = amount.min(balance);
                BigDecimal credit = amount.subtract(applied);
                String sql = "INSERT INTO payments(charge_id, payer_name, paid_at, amount, applied_amount, method, reference, note, recorded_by)"
                        + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                long paymentId;
                try (PreparedStatement insert = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    insert.setLong(1, chargeId);
                    insert.setString(2, payerName);
                    insert.setTimestamp(3, Timestamp.valueOf(paidOn.atStartOfDay()));
                    insert.setBigDecimal(4, amount);
                    insert.setBigDecimal(5, applied);
                    insert.setString(6, method.name());
                    insert.setString(7, blankToNull(reference));
                    insert.setString(8, blankToNull(note));
                    insert.setLong(9, userId);
                    insert.executeUpdate();
                    try (ResultSet keys = insert.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Kh\u00f4ng t\u1ea1o \u0111\u01b0\u1ee3c phi\u1ebfu thu.");
                        paymentId = keys.getLong(1);
                    }
                }
                if (credit.signum() > 0) {
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO apartment_credits(apartment_id, source_payment_id, amount, remaining_amount) VALUES (?, ?, ?, ?)")) {
                        insert.setLong(1, apartmentId);
                        insert.setLong(2, paymentId);
                        insert.setBigDecimal(3, credit);
                        insert.setBigDecimal(4, credit);
                        insert.executeUpdate();
                    }
                }
                connection.commit();
                return new PaymentReceipt(amount, applied, credit);
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
                       pm.paid_at, pm.payer_name, pm.amount, pm.applied_amount,
                       pm.method, pm.reference, pm.note, u.display_name
                FROM payments pm
                JOIN charges c ON c.id = pm.charge_id
                JOIN apartments a ON a.id = c.apartment_id
                JOIN fee_types f ON f.id = c.fee_type_id
                JOIN billing_periods p ON p.id = c.billing_period_id
                JOIN app_users u ON u.id = pm.recorded_by
                WHERE (? = '' OR a.code LIKE ? OR a.household_name LIKE ? OR f.name LIKE ? OR pm.payer_name LIKE ? OR pm.reference LIKE ? OR pm.note LIKE ?)
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
            query.setString(7, pattern);
            try (ResultSet result = query.executeQuery()) {
                while (result.next()) {
                    Timestamp paidAt = result.getTimestamp("paid_at");
                    payments.add(new PaymentRecord(result.getLong("id"), result.getString("code"),
                            result.getString("household_name"), result.getString("fee_name"),
                            result.getString("period_label"), paidAt.toLocalDateTime(),
                            result.getString("payer_name"), result.getBigDecimal("amount"),
                            result.getBigDecimal("applied_amount"), PaymentMethod.valueOf(result.getString("method")),
                            result.getString("reference"), result.getString("note"), result.getString("display_name")));
                }
            }
        }
        return payments;
    }

    public List<ContributionRecord> listContributions() throws SQLException {
        String sql = """
                SELECT c.id, a.code, a.household_name, f.name AS fee_name, c.received_on,
                       c.amount, c.method, c.reference, c.note, u.display_name
                FROM contributions c
                JOIN apartments a ON a.id = c.apartment_id
                JOIN fee_types f ON f.id = c.fee_type_id
                JOIN app_users u ON u.id = c.recorded_by
                ORDER BY c.received_on DESC, c.id DESC
                """;
        List<ContributionRecord> records = new ArrayList<>();
        try (Connection connection = database.openConnection(); Statement query = connection.createStatement();
             ResultSet result = query.executeQuery(sql)) {
            while (result.next()) records.add(new ContributionRecord(result.getLong("id"),
                    result.getString("code"), result.getString("household_name"), result.getString("fee_name"),
                    result.getDate("received_on").toLocalDate(), result.getBigDecimal("amount"),
                    PaymentMethod.valueOf(result.getString("method")), result.getString("reference"),
                    result.getString("note"), result.getString("display_name")));
        }
        return records;
    }

    public void recordContribution(Contribution contribution, long userId) throws SQLException {
        String sql = """
                INSERT INTO contributions(fee_type_id, apartment_id, received_on, amount, method, reference, note, recorded_by)
                SELECT f.id, a.id, ?, ?, ?, ?, ?, ?
                FROM fee_types f JOIN apartments a ON a.id = ?
                WHERE f.id = ? AND f.required = FALSE AND f.active = TRUE AND a.active = TRUE
                """;
        try (Connection connection = database.openConnection(); PreparedStatement insert = connection.prepareStatement(sql)) {
            insert.setDate(1, Date.valueOf(contribution.receivedOn()));
            insert.setBigDecimal(2, contribution.amount());
            insert.setString(3, contribution.method().name());
            insert.setString(4, blankToNull(contribution.reference()));
            insert.setString(5, blankToNull(contribution.note()));
            insert.setLong(6, userId);
            insert.setLong(7, contribution.apartmentId());
            insert.setLong(8, contribution.feeTypeId());
            if (insert.executeUpdate() != 1) throw new SQLException("Khoản đóng góp hoặc căn hộ không còn hiệu lực.");
        }
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

    private record ChargeInput(long apartmentId, BigDecimal areaM2, int residentCount, long feeId,
                               CalculationBasis basis, BigDecimal unitPrice) {}
}
