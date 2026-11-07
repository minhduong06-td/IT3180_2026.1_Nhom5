package com.bluemoon.dao;

import com.bluemoon.config.Database;
import com.bluemoon.config.DatabaseBootstrap;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.ChargeSummary;
import com.bluemoon.model.Domain.CalculationBasis;
import com.bluemoon.model.Domain.Contribution;
import com.bluemoon.model.Domain.Gender;
import com.bluemoon.model.Domain.HouseholdRelationship;
import com.bluemoon.model.Domain.Apartment;
import com.bluemoon.model.Domain.OccupancyStatus;
import com.bluemoon.model.Domain.PaymentMethod;
import com.bluemoon.model.Domain.PaymentReceipt;
import com.bluemoon.model.Domain.Resident;
import com.bluemoon.model.Domain.ResidentRequest;
import com.bluemoon.model.Domain.ResidentRequestInput;
import com.bluemoon.model.Domain.RequestCategory;
import com.bluemoon.model.Domain.RequestStatus;
import com.bluemoon.model.Domain.Role;
import com.bluemoon.model.Domain.UtilityBillInput;
import com.bluemoon.model.Domain.UtilityType;
import com.bluemoon.model.Domain.Vehicle;
import com.bluemoon.model.Domain.VehicleType;
import com.bluemoon.service.AccountLockedException;
import com.bluemoon.service.AccountService;
import com.bluemoon.service.ApartmentService;
import com.bluemoon.service.AuthService;
import com.bluemoon.service.ContributionService;
import com.bluemoon.service.CollectionService;
import com.bluemoon.service.DashboardService;
import com.bluemoon.service.FeeService;
import com.bluemoon.service.HouseholdHeadExistsException;
import com.bluemoon.service.ResidentService;
import com.bluemoon.service.ResidentRequestService;
import com.bluemoon.service.UtilityService;
import com.bluemoon.service.VehicleService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MySqlBillingFlowTest {
    @Test
    void migratesPaymentReceiptsAndAppliesHouseholdCredits() throws Exception {
        String url = System.getenv("BLUEMOON_IT_DB_URL");
        Assumptions.assumeTrue(url != null && !url.isBlank(), "Set BLUEMOON_IT_DB_URL to run MySQL integration checks.");
        Database database = new Database(url, System.getenv("BLUEMOON_IT_DB_USER"), System.getenv("BLUEMOON_IT_DB_PASSWORD"));
        if (Boolean.parseBoolean(System.getenv("BLUEMOON_IT_DB_LEGACY"))) createLegacyApartments(database);
        new DatabaseBootstrap(database).initialize();
        AppRepository repository = new AppRepository(database);
        var firstApartment = repository.listApartments().getFirst();
        assertEquals(1, firstApartment.floor());
        if (Boolean.parseBoolean(System.getenv("BLUEMOON_IT_DB_LEGACY"))) {
            assertEquals("HK-000001", firstApartment.householdCode());
        }
        long adminId = userId(database, "admin");
        long accountantId = userId(database, "accountant");
        long serviceFeeId = feeId(database, "SERVICE");
        String contributionCode = "FUND_IT_CHECK";
        removeFee(database, contributionCode);
        LocalDate laterRateDate = LocalDate.of(2098, 1, 15);
        String plateSuffix = "%05d".formatted(System.currentTimeMillis() % 100_000);
        String motorcyclePlate = "30A-" + plateSuffix;
        String carPlate = "30B-" + plateSuffix;
        removePeriod(database, 2097, 12);
        removePeriod(database, 2098, 2);
        removePeriod(database, 2098, 1);
        removeUtilityBill(database, 2098, 1, firstApartment.id());
        removeVehicles(database, motorcyclePlate, carPlate);
        removeRate(database, serviceFeeId, laterRateDate);
        addRate(database, serviceFeeId, new BigDecimal("9999"), laterRateDate, adminId);

        try {
            AuthService auth = new AuthService(repository);
            AppUser admin = auth.login("admin", "admin123");
            assertEquals("ADMIN", admin.role().name());
            assertNull(auth.login("admin", "wrong-password"));
            verifyAccountManagement(database, repository, auth, admin);
            verifyApartmentManagement(database, repository, admin);
            verifyResidentManagement(database, repository, admin);
            verifyResidentRequestManagement(database, repository, admin);
            FeeService fees = new FeeService(repository);
            fees.create(admin, contributionCode, "Quỹ kiểm tra", false,
                    CalculationBasis.VOLUNTARY, null, null);
            long contributionFeeId = feeId(database, contributionCode);
            var apartment = repository.listApartments().getFirst();
            ContributionService contributions = new ContributionService(repository);
            Contribution contribution = new Contribution(contributionFeeId, apartment.id(),
                    new BigDecimal("50000"), LocalDate.now(), PaymentMethod.CASH, "FUND-001", null);
            fees.setActive(admin, contributionFeeId, false);
            assertThrows(IllegalArgumentException.class, () -> contributions.record(admin, contribution));
            fees.setActive(admin, contributionFeeId, true);
            contributions.record(admin, contribution);
            assertTrue(repository.listContributions().stream().anyMatch(row -> row.feeName().equals("Quỹ kiểm tra")
                    && row.amount().compareTo(new BigDecimal("50000")) == 0));
            UtilityService utilities = new UtilityService(repository);
            utilities.save(admin, new UtilityBillInput(null, 2098, 1, apartment.id(), UtilityType.ELECTRICITY,
                    new BigDecimal("350000"), "EVN", "EVN-2098-A101"));
            UtilityBillInput savedBill = utilities.list(admin).stream()
                    .filter(bill -> bill.year() == 2098 && bill.month() == 1 && bill.apartmentId() == apartment.id())
                    .map(bill -> new UtilityBillInput(bill.id(), bill.year(), bill.month(), bill.apartmentId(),
                            bill.type(), bill.amount(), bill.provider(), bill.reference()))
                    .findFirst().orElseThrow();
            assertEquals(new BigDecimal("350000"), savedBill.amount());
            VehicleService vehicles = new VehicleService(repository);
            vehicles.register(admin, apartment.id(), VehicleType.MOTORCYCLE, motorcyclePlate);
            vehicles.register(admin, apartment.id(), VehicleType.CAR, carPlate);
            List<Vehicle> testVehicles = vehicles.list(admin).stream()
                    .filter(vehicle -> vehicle.licensePlate().equals(motorcyclePlate)
                            || vehicle.licensePlate().equals(carPlate)).toList();
            assertEquals(2, testVehicles.size());
            assertThrows(IllegalArgumentException.class, () -> vehicles.register(admin, apartment.id(),
                    VehicleType.CAR, motorcyclePlate));
            assertThrows(IllegalArgumentException.class, () -> vehicles.register(admin, apartment.id(),
                    VehicleType.MOTORCYCLE, "biển số sai"));
            var period = repository.issuePeriod(2098, 1, LocalDate.of(2098, 1, 20),
                    LocalDate.of(2098, 1, 31), adminId);
            assertEquals(16, period.chargeCount());
            assertEquals(new BigDecimal("5623500"), period.totalAmount());
            assertTrue(utilities.list(admin).stream().anyMatch(bill -> bill.id() == savedBill.id() && bill.issued()));
            assertThrows(SQLException.class, () -> utilities.save(admin, new UtilityBillInput(savedBill.id(),
                    savedBill.year(), savedBill.month(), savedBill.apartmentId(), savedBill.type(),
                    new BigDecimal("360000"), savedBill.provider(), savedBill.reference())));
            var initialDebts = repository.listHouseholdDebts(period.id());
            assertEquals(3, initialDebts.size());
            var firstDebt = initialDebts.stream().filter(debt -> debt.apartmentCode().equals("A101"))
                    .findFirst().orElseThrow();
            assertEquals(1, firstDebt.floor());
            assertEquals(0, firstDebt.residentCount());
            assertEquals(new BigDecimal("2725000"), firstDebt.totalDue());
            assertEquals("UNPAID", firstDebt.status().name());
            CollectionService collections = new CollectionService(repository);
            assertEquals(3, collections.debts(admin, period.id(), 1, 1, null).size());
            assertTrue(collections.debts(admin, period.id(), 2, 3, null).isEmpty());
            assertThrows(IllegalArgumentException.class, () -> collections.debts(admin, period.id(), 10, 5, null));
            assertThrows(SQLException.class, () -> repository.issuePeriod(2098, 1,
                    LocalDate.of(2098, 1, 1), LocalDate.of(2098, 1, 31), adminId));

            ChargeSummary serviceCharge = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> "Phí dịch vụ".equals(charge.feeName())).findFirst().orElseThrow();
            assertEquals(new BigDecimal("650000"), serviceCharge.amount());
            ChargeSummary sanitationCharge = repository.listCharges(period.id(), "A102").stream()
                    .filter(charge -> "Phí vệ sinh".equals(charge.feeName())).findFirst().orElseThrow();
            assertEquals(BigDecimal.ZERO, sanitationCharge.amount());
            assertEquals("PAID", sanitationCharge.status().name());
            ChargeSummary electricityCharge = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> "Tiền điện".equals(charge.feeName())).findFirst().orElseThrow();
            assertEquals(new BigDecimal("350000"), electricityCharge.amount());
            assertEquals(CalculationBasis.EXTERNAL_BILL, electricityCharge.basis());
            ChargeSummary motorcycleCharge = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> "Phí gửi xe máy".equals(charge.feeName())).findFirst().orElseThrow();
            assertEquals(new BigDecimal("70000"), motorcycleCharge.amount());
            ChargeSummary carCharge = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> "Phí gửi ô tô".equals(charge.feeName())).findFirst().orElseThrow();
            assertEquals(new BigDecimal("1200000"), carCharge.amount());
            for (Vehicle vehicle : testVehicles) vehicles.deactivate(admin, vehicle.id());
            assertTrue(vehicles.list(admin).stream().filter(vehicle -> testVehicles.stream()
                    .anyMatch(testVehicle -> testVehicle.id() == vehicle.id())).noneMatch(Vehicle::active));
            assertEquals(new BigDecimal("70000"), repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> "Phí gửi xe máy".equals(charge.feeName())).findFirst().orElseThrow().amount());
            var dashboard = new DashboardService(repository).summary(admin);
            assertEquals("01/2098", dashboard.period());
            assertEquals(new BigDecimal("5623500"), dashboard.totalAmount());
            assertEquals(3, dashboard.apartmentCount());
            AppUser accountant = new AppUser(accountantId, "accountant", "Kế toán", Role.ACCOUNTANT);
            simulateLegacyPayment(database, serviceCharge.id(), LocalDate.now().minusDays(1),
                    new BigDecimal("100000"), accountantId);
            assertTrue(hasCheckConstraint(database, "payments", "chk_payment_applied"));
            var migratedReceipt = repository.listPayments("A101").getFirst();
            assertEquals("Chưa ghi nhận", migratedReceipt.payerName());
            assertEquals(new BigDecimal("100000"), migratedReceipt.appliedAmount());
            assertEquals(LocalDate.now().minusDays(1), migratedReceipt.paidAt().toLocalDate());
            ChargeSummary updated = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> charge.id() == serviceCharge.id()).findFirst().orElseThrow();
            assertEquals(new BigDecimal("550000"), updated.balance());
            assertEquals("PARTIALLY_PAID", updated.status().name());
            var updatedDebt = repository.listHouseholdDebts(period.id()).stream()
                    .filter(debt -> debt.apartmentCode().equals("A101")).findFirst().orElseThrow();
            assertEquals(new BigDecimal("100000"), updatedDebt.paid());
            assertEquals(new BigDecimal("2625000"), updatedDebt.balance());
            assertEquals("PARTIALLY_PAID", updatedDebt.status().name());
            assertNotNull(updatedDebt.lastPaidAt());
            PaymentReceipt receipt = collections.recordPayment(accountant, serviceCharge.id(),
                    new BigDecimal("600000"), "Nguyễn An", LocalDate.now(), PaymentMethod.BANK_TRANSFER,
                    "BM-2098-A101", null);
            assertEquals(new BigDecimal("600000"), receipt.received());
            assertEquals(new BigDecimal("550000"), receipt.applied());
            assertEquals(new BigDecimal("50000"), receipt.credit());
            assertThrows(IllegalArgumentException.class, () -> collections.recordPayment(accountant,
                    serviceCharge.id(), BigDecimal.ONE, "Nguyễn An", LocalDate.now().plusDays(1),
                    PaymentMethod.CASH, null, null));
            ChargeSummary paid = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> charge.id() == serviceCharge.id()).findFirst().orElseThrow();
            assertEquals(BigDecimal.ZERO, paid.balance());
            assertEquals("PAID", paid.status().name());
            var currentDebt = repository.listHouseholdDebts(period.id()).stream()
                    .filter(debt -> debt.apartmentCode().equals("A101")).findFirst().orElseThrow();
            assertEquals(new BigDecimal("650000"), currentDebt.paid());
            assertEquals(new BigDecimal("50000"), currentDebt.creditBalance());
            var receipts = repository.listPayments("A101");
            assertEquals(2, receipts.size());
            assertEquals("Nguyễn An", receipts.getFirst().payerName());
            assertEquals(new BigDecimal("600000"), receipts.getFirst().amount());
            assertEquals(new BigDecimal("550000"), receipts.getFirst().appliedAmount());
            assertEquals(LocalDate.now(), receipts.getFirst().paidAt().toLocalDate());
            assertEquals("Chưa ghi nhận", receipts.getLast().payerName());
            var paidDashboard = new DashboardService(repository).summary(admin);
            assertEquals(new BigDecimal("650000"), paidDashboard.paidAmount());
            assertEquals(new BigDecimal("4973500"), paidDashboard.balance());

            var previousPeriod = repository.issuePeriod(2097, 12, LocalDate.of(2097, 12, 20),
                    LocalDate.of(2097, 12, 31), adminId);
            var previousDebt = repository.listHouseholdDebts(previousPeriod.id()).stream()
                    .filter(debt -> debt.apartmentCode().equals("A101")).findFirst().orElseThrow();
            assertEquals(BigDecimal.ZERO, previousDebt.paid());
            assertEquals(previousDebt.totalDue(), previousDebt.balance());
            assertEquals(new BigDecimal("50000"), previousDebt.creditBalance());

            var followingPeriod = repository.issuePeriod(2098, 2, LocalDate.of(2098, 2, 20),
                    LocalDate.of(2098, 2, 28), adminId);
            var followingDebt = repository.listHouseholdDebts(followingPeriod.id()).stream()
                    .filter(debt -> debt.apartmentCode().equals("A101")).findFirst().orElseThrow();
            assertEquals(new BigDecimal("50000"), followingDebt.paid());
            assertEquals(followingDebt.totalDue().subtract(new BigDecimal("50000")), followingDebt.balance());
            assertEquals(BigDecimal.ZERO, followingDebt.creditBalance());
        } finally {
            removePeriod(database, 2097, 12);
            removePeriod(database, 2098, 2);
            removePeriod(database, 2098, 1);
            removeUtilityBill(database, 2098, 1, firstApartment.id());
            removeVehicles(database, motorcyclePlate, carPlate);
            removeRate(database, serviceFeeId, laterRateDate);
            removeFee(database, contributionCode);
        }
    }

    private static void verifyApartmentManagement(Database database, AppRepository repository, AppUser admin) throws Exception {
        ApartmentService apartments = new ApartmentService(repository);
        Apartment apartment = new Apartment(null, "P0508", "HK-0508", new BigDecimal("80.25"),
                "Nguyễn Thị An", "0900000508", LocalDate.now().minusDays(1), OccupancyStatus.VACANT, true);
        try {
            apartments.save(admin, apartment);
            Apartment saved = repository.listApartments().stream().filter(value -> value.code().equals("P0508"))
                    .findFirst().orElseThrow();
            assertEquals(5, saved.floor());
            assertEquals("HK-0508", saved.householdCode());
            assertEquals(OccupancyStatus.VACANT, saved.occupancyStatus());
            assertThrows(IllegalArgumentException.class, () -> apartments.save(admin,
                    new Apartment(null, "P0509", "HK-0508", BigDecimal.ONE, "Trần Văn A", "0900000509",
                            LocalDate.now(), OccupancyStatus.OCCUPIED, true)));
            assertThrows(IllegalArgumentException.class, () -> apartments.save(admin,
                    new Apartment(null, "P0001", "HK-0001", BigDecimal.ONE, "Trần Văn A", "0900000001",
                            LocalDate.now(), OccupancyStatus.OCCUPIED, true)));
        } finally {
            try (Connection connection = database.openConnection();
                 PreparedStatement delete = connection.prepareStatement("DELETE FROM apartments WHERE code = 'P0508'")) {
                delete.executeUpdate();
            }
        }
    }

    private static void createLegacyApartments(Database database) throws SQLException {
        try (Connection connection = database.openConnection(); Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE apartments (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        code VARCHAR(20) NOT NULL UNIQUE,
                        area_m2 DECIMAL(10, 2) NOT NULL,
                        household_name VARCHAR(120) NOT NULL,
                        contact_phone VARCHAR(32) NOT NULL,
                        active BOOLEAN NOT NULL DEFAULT TRUE,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        CONSTRAINT chk_apartment_area CHECK (area_m2 > 0)
                    )
                    """);
            statement.executeUpdate("INSERT INTO apartments(code, area_m2, household_name, contact_phone) VALUES "
                    + "('A101', 65.00, 'Nguyễn Minh An', '0900000101'), "
                    + "('A102', 78.50, 'Trần Gia Bảo', '0900000102'), "
                    + "('A103', 92.00, 'Lê Thu Hà', '0900000103')");
        }
    }

    private static void verifyResidentManagement(Database database, AppRepository repository, AppUser admin) throws Exception {
        var apartment = repository.listApartments().stream().filter(value -> value.code().equals("A101")).findFirst().orElseThrow();
        ResidentService residents = new ResidentService(repository);
        String firstId = "%012d".formatted(System.currentTimeMillis() % 1_000_000_000_000L);
        String secondId = "%012d".formatted((System.currentTimeMillis() + 1) % 1_000_000_000_000L);
        try {
            Resident head = new Resident(null, apartment.id(), "Đỗ Gia Ngọc", LocalDate.now().minusYears(30),
                    Gender.FEMALE, firstId, HouseholdRelationship.HEAD, LocalDate.now().minusYears(1));
            residents.save(admin, head, false);
            Resident child = new Resident(null, apartment.id(), "Đỗ Minh An", LocalDate.now().minusYears(10),
                    Gender.MALE, null, HouseholdRelationship.CHILD, LocalDate.now().minusYears(2));
            residents.save(admin, child, false);
            Resident nextHead = new Resident(null, apartment.id(), "Trần Thị Bảo", LocalDate.now().minusYears(28),
                    Gender.FEMALE, secondId, HouseholdRelationship.HEAD, LocalDate.now().minusYears(1));
            assertThrows(HouseholdHeadExistsException.class, () -> residents.save(admin, nextHead, false));
            residents.save(admin, nextHead, true);
            assertTrue(residents.list(admin, apartment.id()).stream()
                    .anyMatch(value -> value.fullName().equals("Đỗ Gia Ngọc") && value.relationship() == HouseholdRelationship.OTHER));
            assertTrue(residents.search(admin, "ngoc").stream()
                    .anyMatch(entry -> entry.resident() != null && entry.resident().fullName().equals("Đỗ Gia Ngọc")));
            assertTrue(residents.search(admin, firstId).stream()
                    .anyMatch(entry -> entry.resident() != null && entry.resident().nationalId().equals(firstId)));
            assertThrows(IllegalArgumentException.class, () -> residents.save(admin,
                    new Resident(null, apartment.id(), "Người lớn", LocalDate.now().minusYears(30), Gender.MALE,
                            null, HouseholdRelationship.OTHER, LocalDate.now()), false));
        } finally {
            try (Connection connection = database.openConnection();
                 PreparedStatement delete = connection.prepareStatement("DELETE FROM residents WHERE apartment_id = ?")) {
                delete.setLong(1, apartment.id());
                delete.executeUpdate();
            }
            try (Connection connection = database.openConnection();
                 PreparedStatement update = connection.prepareStatement(
                         "UPDATE apartments SET household_name = 'Nguyễn Minh An' WHERE id = ?")) {
                update.setLong(1, apartment.id());
                update.executeUpdate();
            }
        }
    }

    private static void verifyResidentRequestManagement(Database database, AppRepository repository,
                                                        AppUser admin) throws Exception {
        var apartment = repository.listApartments().getFirst();
        ResidentRequestService requests = new ResidentRequestService(repository);
        String subject = "Yêu cầu bảo trì " + System.currentTimeMillis();
        try {
            requests.save(admin, new ResidentRequestInput(null, apartment.id(), "Nguyễn Minh An",
                    RequestCategory.MAINTENANCE, subject, "Vòi bếp nhỏ nước liên tục.", RequestStatus.RECEIVED, null));
            ResidentRequest created = requests.list(admin).stream()
                    .filter(request -> request.subject().equals(subject)).findFirst().orElseThrow();
            assertEquals(apartment.code(), created.apartmentCode());
            assertEquals(RequestStatus.RECEIVED, created.status());
            assertNotNull(created.createdAt());
            assertThrows(IllegalArgumentException.class, () -> requests.save(admin,
                    new ResidentRequestInput(null, apartment.id(), "Nguyễn Minh An", RequestCategory.MAINTENANCE,
                            subject + " mới", "Yêu cầu mới không thể được đánh dấu đang xử lý.",
                            RequestStatus.IN_PROGRESS, null)));
            requests.save(admin, new ResidentRequestInput(created.id(), apartment.id(), created.reporterName(),
                    created.category(), created.subject(), created.description(), RequestStatus.IN_PROGRESS,
                    "Đã phân công kỹ thuật viên."));
            ResidentRequest updated = requests.list(admin).stream()
                    .filter(request -> request.id() == created.id()).findFirst().orElseThrow();
            assertEquals(RequestStatus.IN_PROGRESS, updated.status());
            assertEquals("Đã phân công kỹ thuật viên.", updated.handlingNote());
            assertNotNull(updated.updatedAt());
        } finally {
            try (Connection connection = database.openConnection();
                 PreparedStatement delete = connection.prepareStatement(
                         "DELETE FROM resident_requests WHERE apartment_id = ? AND subject = ?")) {
                delete.setLong(1, apartment.id());
                delete.setString(2, subject);
                delete.executeUpdate();
            }
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

    private static void removeFee(Database database, String code) throws SQLException {
        try (Connection connection = database.openConnection()) {
            try (PreparedStatement deleteContributions = connection.prepareStatement(
                    "DELETE c FROM contributions c JOIN fee_types f ON f.id = c.fee_type_id WHERE f.code = ?")) {
                deleteContributions.setString(1, code);
                deleteContributions.executeUpdate();
            }
            try (PreparedStatement deleteFee = connection.prepareStatement(
                    "DELETE FROM fee_types WHERE code = ? AND id NOT IN (SELECT fee_type_id FROM charges)")) {
                deleteFee.setString(1, code);
                deleteFee.executeUpdate();
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

    private static void simulateLegacyPayment(Database database, long chargeId, LocalDate paidOn,
                                              BigDecimal amount, long userId) throws Exception {
        try (Connection connection = database.openConnection()) {
            try (Statement alter = connection.createStatement()) {
                alter.executeUpdate("ALTER TABLE payments DROP CHECK chk_payment_applied,"
                        + " DROP COLUMN payer_name, DROP COLUMN applied_amount");
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO payments(charge_id, paid_at, amount, method, reference, note, recorded_by)"
                            + " VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                insert.setLong(1, chargeId);
                insert.setTimestamp(2, java.sql.Timestamp.valueOf(paidOn.atStartOfDay()));
                insert.setBigDecimal(3, amount);
                insert.setString(4, PaymentMethod.CASH.name());
                insert.setString(5, null);
                insert.setString(6, "Legacy payment");
                insert.setLong(7, userId);
                insert.executeUpdate();
            }
        }
        new DatabaseBootstrap(database).initialize();
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
                    try (PreparedStatement deleteApplications = connection.prepareStatement(
                            "DELETE FROM credit_applications WHERE charge_id IN (SELECT id FROM charges WHERE billing_period_id = ?)"
                                    + " OR credit_id IN (SELECT id FROM apartment_credits WHERE source_payment_id IN"
                                    + " (SELECT id FROM payments WHERE charge_id IN (SELECT id FROM charges WHERE billing_period_id = ?)))")) {
                        deleteApplications.setLong(1, id);
                        deleteApplications.setLong(2, id);
                        deleteApplications.executeUpdate();
                    }
                    try (PreparedStatement deleteCredits = connection.prepareStatement(
                            "DELETE FROM apartment_credits WHERE source_payment_id IN"
                                    + " (SELECT id FROM payments WHERE charge_id IN (SELECT id FROM charges WHERE billing_period_id = ?))")) {
                        deleteCredits.setLong(1, id);
                        deleteCredits.executeUpdate();
                    }
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

    private static void removeUtilityBill(Database database, int year, int month, long apartmentId) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement delete = connection.prepareStatement(
                     "DELETE FROM utility_bills WHERE period_year = ? AND period_month = ? AND apartment_id = ? AND utility_type = 'ELECTRICITY'")) {
            delete.setInt(1, year);
            delete.setInt(2, month);
            delete.setLong(3, apartmentId);
            delete.executeUpdate();
        }
    }

    private static void removeVehicles(Database database, String motorcyclePlate, String carPlate) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement delete = connection.prepareStatement(
                     "DELETE FROM vehicles WHERE license_plate IN (?, ?)")) {
            delete.setString(1, motorcyclePlate);
            delete.setString(2, carPlate);
            delete.executeUpdate();
        }
    }

    private static boolean hasCheckConstraint(Database database, String table, String constraint) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement query = connection.prepareStatement(
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
