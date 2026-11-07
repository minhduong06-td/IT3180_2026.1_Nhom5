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
        boolean legacy = Boolean.parseBoolean(System.getenv("BLUEMOON_IT_DB_LEGACY"));
        if (legacy) createLegacyApartments(database);
        new DatabaseBootstrap(database).initialize();
        AppRepository repository = new AppRepository(database);
        AccountService accounts = new AccountService(repository);
        if (legacy) {
            assertEquals(3, repository.listApartments().size());
            assertEquals(0, residentCount(repository));
            assertTrue(repository.listFeeTypes().isEmpty());
            assertTrue(!repository.hasUsers());
            assertEquals("ADMIN", accounts.createInitialAdmin("admin", "Quản trị", "admin123", "admin123")
                    .role().name());
            return;
        }
        assertTrue(repository.listApartments().isEmpty());
        assertTrue(repository.listFeeTypes().isEmpty());
        assertTrue(!repository.hasUsers());
        new DatabaseBootstrap(database).initialize();
        assertTrue(repository.listApartments().isEmpty());
        assertTrue(repository.listFeeTypes().isEmpty());
        assertTrue(!repository.hasUsers());
        assertThrows(IllegalArgumentException.class, () -> accounts.createInitialAdmin(
                "admin", "Quản trị", "short", "short"));
        AppUser initialAdmin = accounts.createInitialAdmin("admin", "Quản trị BlueMoon", "admin123", "admin123");
        assertTrue(repository.hasUsers());
        assertThrows(IllegalStateException.class, () -> accounts.createInitialAdmin(
                "second-admin", "Tài khoản khác", "password123", "password123"));
        accounts.create(initialAdmin, "le.thu.ngan", "Kế toán BlueMoon", Role.ACCOUNTANT,
                "accountant123", "accountant123");
        var emptySummary = new DashboardService(repository).summary(initialAdmin);
        assertEquals("Chưa phát hành", emptySummary.period());
        assertEquals(BigDecimal.ZERO, emptySummary.totalAmount());
        assertEquals(0, emptySummary.apartmentCount());
        createBillingFixture(database, initialAdmin.id());
        var firstApartment = repository.listApartments().getFirst();
        assertEquals(1, firstApartment.floor());
        assertEquals(8, repository.listApartments().size());
        assertEquals(18, residentCount(repository));
        long adminId = userId(database, "admin");
        long accountantId = userId(database, "le.thu.ngan");
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
            assertEquals(2, fees.list(admin).stream().filter(fee -> fee.required() && fee.active()).count());
            assertTrue(fees.list(admin).stream().filter(fee -> fee.required() && fee.active())
                    .allMatch(fee -> fee.code().equals("SERVICE") || fee.code().equals("MANAGEMENT")));
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
            // Enable optional fee types for this billing fixture.
            fees.setActive(admin, feeId(database, "SANITATION"), true);
            fees.setActive(admin, feeId(database, "MOTORCYCLE_PARKING"), true);
            fees.setActive(admin, feeId(database, "CAR_PARKING"), true);
            var period = repository.issuePeriod(2098, 1, LocalDate.of(2098, 1, 20),
                    LocalDate.of(2098, 1, 31), adminId);
            assertEquals(41, period.chargeCount());
            assertEquals(new BigDecimal("11889154"), period.totalAmount());
            assertTrue(utilities.list(admin).stream().anyMatch(bill -> bill.id() == savedBill.id() && bill.issued()));
            assertThrows(SQLException.class, () -> utilities.save(admin, new UtilityBillInput(savedBill.id(),
                    savedBill.year(), savedBill.month(), savedBill.apartmentId(), savedBill.type(),
                    new BigDecimal("360000"), savedBill.provider(), savedBill.reference())));
            var initialDebts = repository.listHouseholdDebts(period.id());
            assertEquals(8, initialDebts.size());
            var firstDebt = initialDebts.stream().filter(debt -> debt.apartmentCode().equals("A101"))
                    .findFirst().orElseThrow();
            assertEquals(1, firstDebt.floor());
            assertEquals(3, firstDebt.residentCount());
            assertEquals(new BigDecimal("2453952"), firstDebt.totalDue());
            assertEquals("UNPAID", firstDebt.status().name());
            CollectionService collections = new CollectionService(repository);
            assertEquals(8, collections.debts(admin, period.id(), 1, 1, null).size());
            assertTrue(collections.debts(admin, period.id(), 2, 3, null).isEmpty());
            assertThrows(IllegalArgumentException.class, () -> collections.debts(admin, period.id(), 10, 5, null));
            assertThrows(SQLException.class, () -> repository.issuePeriod(2098, 1,
                    LocalDate.of(2098, 1, 1), LocalDate.of(2098, 1, 31), adminId));

            ChargeSummary serviceCharge = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> "Phí dịch vụ".equals(charge.feeName())).findFirst().orElseThrow();
            assertEquals(new BigDecimal("479952"), serviceCharge.amount());
            ChargeSummary sanitationCharge = repository.listCharges(period.id(), "A102").stream()
                    .filter(charge -> "Phí vệ sinh".equals(charge.feeName())).findFirst().orElseThrow();
            assertEquals(new BigDecimal("12000"), sanitationCharge.amount());
            assertEquals("UNPAID", sanitationCharge.status().name());
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
            assertEquals(new BigDecimal("11889154"), dashboard.totalAmount());
            assertEquals(8, dashboard.apartmentCount());
            AppUser accountant = new AppUser(accountantId, "le.thu.ngan", "Kế toán", Role.ACCOUNTANT);
            simulateLegacyPayment(database, serviceCharge.id(), LocalDate.now().minusDays(1),
                    new BigDecimal("100000"), accountantId);
            assertTrue(hasCheckConstraint(database, "payments", "chk_payment_applied"));
            var migratedReceipt = repository.listPayments("A101").getFirst();
            assertEquals("Chưa ghi nhận", migratedReceipt.payerName());
            assertEquals(new BigDecimal("100000"), migratedReceipt.appliedAmount());
            assertEquals(LocalDate.now().minusDays(1), migratedReceipt.paidAt().toLocalDate());
            ChargeSummary updated = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> charge.id() == serviceCharge.id()).findFirst().orElseThrow();
            assertEquals(new BigDecimal("379952"), updated.balance());
            assertEquals("PARTIALLY_PAID", updated.status().name());
            var updatedDebt = repository.listHouseholdDebts(period.id()).stream()
                    .filter(debt -> debt.apartmentCode().equals("A101")).findFirst().orElseThrow();
            assertEquals(new BigDecimal("100000"), updatedDebt.paid());
            assertEquals(new BigDecimal("2353952"), updatedDebt.balance());
            assertEquals("PARTIALLY_PAID", updatedDebt.status().name());
            assertNotNull(updatedDebt.lastPaidAt());
            PaymentReceipt receipt = collections.recordPayment(accountant, serviceCharge.id(),
                    new BigDecimal("600000"), "Nguyễn An", LocalDate.now(), PaymentMethod.BANK_TRANSFER,
                    "BM-2098-A101", null);
            assertEquals(new BigDecimal("600000"), receipt.received());
            assertEquals(new BigDecimal("379952"), receipt.applied());
            assertEquals(new BigDecimal("220048"), receipt.credit());
            assertThrows(IllegalArgumentException.class, () -> collections.recordPayment(accountant,
                    serviceCharge.id(), BigDecimal.ONE, "Nguyễn An", LocalDate.now().plusDays(1),
                    PaymentMethod.CASH, null, null));
            ChargeSummary paid = repository.listCharges(period.id(), "A101").stream()
                    .filter(charge -> charge.id() == serviceCharge.id()).findFirst().orElseThrow();
            assertEquals(BigDecimal.ZERO, paid.balance());
            assertEquals("PAID", paid.status().name());
            var currentDebt = repository.listHouseholdDebts(period.id()).stream()
                    .filter(debt -> debt.apartmentCode().equals("A101")).findFirst().orElseThrow();
            assertEquals(new BigDecimal("479952"), currentDebt.paid());
            assertEquals(new BigDecimal("220048"), currentDebt.creditBalance());
            var receipts = repository.listPayments("A101");
            assertEquals(2, receipts.size());
            assertEquals("Nguyễn An", receipts.getFirst().payerName());
            assertEquals(new BigDecimal("600000"), receipts.getFirst().amount());
            assertEquals(new BigDecimal("379952"), receipts.getFirst().appliedAmount());
            assertEquals(LocalDate.now(), receipts.getFirst().paidAt().toLocalDate());
            assertEquals("Chưa ghi nhận", receipts.getLast().payerName());
            var paidDashboard = new DashboardService(repository).summary(admin);
            assertEquals(new BigDecimal("479952"), paidDashboard.paidAmount());
            assertEquals(new BigDecimal("11409202"), paidDashboard.balance());

            var previousPeriod = repository.issuePeriod(2097, 12, LocalDate.of(2097, 12, 20),
                    LocalDate.of(2097, 12, 31), adminId);
            var previousDebt = repository.listHouseholdDebts(previousPeriod.id()).stream()
                    .filter(debt -> debt.apartmentCode().equals("A101")).findFirst().orElseThrow();
            assertEquals(BigDecimal.ZERO, previousDebt.paid());
            assertEquals(previousDebt.totalDue(), previousDebt.balance());
            assertEquals(new BigDecimal("220048"), previousDebt.creditBalance());

            var followingPeriod = repository.issuePeriod(2098, 2, LocalDate.of(2098, 2, 20),
                    LocalDate.of(2098, 2, 28), adminId);
            var followingDebt = repository.listHouseholdDebts(followingPeriod.id()).stream()
                    .filter(debt -> debt.apartmentCode().equals("A101")).findFirst().orElseThrow();
            assertEquals(new BigDecimal("220048"), followingDebt.paid());
            assertEquals(followingDebt.totalDue().subtract(new BigDecimal("220048")), followingDebt.balance());
            assertEquals(BigDecimal.ZERO, followingDebt.creditBalance());
        } finally {
            removePeriod(database, 2097, 12);
            removePeriod(database, 2098, 2);
            removePeriod(database, 2098, 1);
            removeUtilityBill(database, 2098, 1, firstApartment.id());
            removeVehicles(database, motorcyclePlate, carPlate);
            setFeeActive(database, "SANITATION", false);
            setFeeActive(database, "MOTORCYCLE_PARKING", false);
            setFeeActive(database, "CAR_PARKING", false);
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

    private static void createBillingFixture(Database database, long adminId) throws SQLException {
        Object[][] apartments = {
                {"A101", "HK-000101", "48.00", "Nguyễn Minh An", "0900000101"},
                {"A102", "HK-000102", "52.50", "Trần Gia Bảo", "0900000102"},
                {"A103", "HK-000103", "65.00", "Lê Thu Hà", "0900000103"},
                {"A104", "HK-000104", "70.00", "Phạm Quốc Huy", "0900000104"},
                {"A105", "HK-000105", "78.50", "Đỗ Mai Anh", "0900000105"},
                {"A106", "HK-000106", "86.25", "Vũ Hoàng Nam", "0900000106"},
                {"A107", "HK-000107", "92.00", "Nguyễn Lan Chi", "0900000107"},
                {"A108", "HK-000108", "105.50", "Bùi Đức Long", "0900000108"}
        };
        try (Connection connection = database.openConnection();
             PreparedStatement insert = connection.prepareStatement("""
                     INSERT INTO apartments(code, household_code, area_m2, household_name, contact_phone, move_in_date)
                     VALUES (?, ?, ?, ?, ?, '2020-01-01')
                     """)) {
            for (Object[] apartment : apartments) {
                for (int index = 0; index < apartment.length; index++) insert.setObject(index + 1, apartment[index]);
                insert.executeUpdate();
            }
        }

        Object[][] residents = {
                {"A101", "Nguyễn Minh An", "1985-03-12", "MALE", "001085000101", "HEAD", "2021-08-01"},
                {"A101", "Phạm Ngọc Mai", "1987-09-24", "FEMALE", "001087000102", "WIFE", "2021-08-01"},
                {"A101", "Nguyễn Minh Khôi", "2015-06-18", "MALE", "015115000103", "CHILD", "2021-08-01"},
                {"A102", "Trần Gia Bảo", "1979-01-30", "MALE", "001079000201", "HEAD", "2022-02-15"},
                {"A102", "Lê Thanh Hương", "1982-11-07", "FEMALE", "001082000202", "WIFE", "2022-02-15"},
                {"A103", "Lê Thu Hà", "1990-04-22", "FEMALE", "001090000301", "HEAD", "2022-05-20"},
                {"A103", "Lê Minh Quân", "1960-12-03", "MALE", "001060000302", "PARENT", "2022-05-20"},
                {"A104", "Phạm Quốc Huy", "1988-07-11", "MALE", "001088000401", "HEAD", "2023-01-10"},
                {"A104", "Đặng Ngọc Linh", "1991-02-16", "FEMALE", "001091000402", "WIFE", "2023-01-10"},
                {"A104", "Phạm Gia Minh", "2018-10-29", "MALE", "018118000403", "CHILD", "2023-01-10"},
                {"A105", "Đỗ Mai Anh", "1986-05-09", "FEMALE", "001086000501", "HEAD", "2021-11-03"},
                {"A105", "Đỗ Khánh Ngân", "2012-08-21", "FEMALE", "012112000502", "CHILD", "2021-11-03"},
                {"A106", "Vũ Hoàng Nam", "1983-10-14", "MALE", "001083000601", "HEAD", "2024-03-12"},
                {"A106", "Nguyễn Hải Yến", "1985-08-05", "FEMALE", "001085000602", "WIFE", "2024-03-12"},
                {"A107", "Nguyễn Lan Chi", "1994-06-27", "FEMALE", "001094000701", "HEAD", "2024-06-01"},
                {"A107", "Nguyễn Văn Lâm", "1965-03-15", "MALE", "001065000702", "PARENT", "2024-06-01"},
                {"A108", "Bùi Đức Long", "1976-09-19", "MALE", "001076000801", "HEAD", "2022-09-18"},
                {"A108", "Bùi Đức Anh", "2004-01-08", "MALE", "004104000802", "TENANT", "2024-09-01"}
        };
        try (Connection connection = database.openConnection();
             PreparedStatement insert = connection.prepareStatement("""
                     INSERT INTO residents(apartment_id, full_name, date_of_birth, gender, national_id, relationship, move_in_date)
                     SELECT id, ?, ?, ?, ?, ?, ? FROM apartments WHERE code = ?
                     """)) {
            for (Object[] resident : residents) {
                for (int index = 1; index < resident.length; index++) insert.setObject(index, resident[index]);
                insert.setObject(7, resident[0]);
                assertEquals(1, insert.executeUpdate());
            }
        }

        Object[][] fees = {
                {"SERVICE", "Phí dịch vụ", true, "PER_M2", true, "10000"},
                {"MANAGEMENT", "Phí quản lý", true, "PER_M2", true, "7000"},
                {"SANITATION", "Phí vệ sinh", true, "PER_RESIDENT", false, "6000"},
                {"MOTORCYCLE_PARKING", "Phí gửi xe máy", true, "PER_MOTORCYCLE", false, "70000"},
                {"CAR_PARKING", "Phí gửi ô tô", true, "PER_CAR", false, "1200000"}
        };
        try (Connection connection = database.openConnection();
             PreparedStatement insertFee = connection.prepareStatement(
                     "INSERT INTO fee_types(code, name, required, calculation_basis, active) VALUES (?, ?, ?, ?, ?)");
             PreparedStatement insertRate = connection.prepareStatement("""
                     INSERT INTO fee_rates(fee_type_id, unit_price, effective_from, created_by)
                     SELECT id, ?, '2026-01-01', ? FROM fee_types WHERE code = ?
                     """)) {
            for (Object[] fee : fees) {
                for (int index = 0; index < 5; index++) insertFee.setObject(index + 1, fee[index]);
                insertFee.executeUpdate();
                insertRate.setBigDecimal(1, new BigDecimal((String) fee[5]));
                insertRate.setLong(2, adminId);
                insertRate.setString(3, (String) fee[0]);
                assertEquals(1, insertRate.executeUpdate());
            }
        }
    }

    private static void verifyResidentManagement(Database database, AppRepository repository, AppUser admin) throws Exception {
        ApartmentService apartments = new ApartmentService(repository);
        apartments.save(admin, new Apartment(null, "T0901", "HK-T0901", new BigDecimal("60.00"),
                "Test household", "0900000901", LocalDate.now().minusDays(1), OccupancyStatus.OCCUPIED, true));
        apartments.save(admin, new Apartment(null, "T0902", "HK-T0902", new BigDecimal("60.00"),
                "Inactive household", "0900000902", LocalDate.now().minusDays(1), OccupancyStatus.VACANT, false));
        var apartment = repository.listApartments().stream().filter(value -> value.code().equals("T0901")).findFirst().orElseThrow();
        var inactiveApartment = repository.listApartments().stream().filter(value -> value.code().equals("T0902")).findFirst().orElseThrow();
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
            assertThrows(IllegalArgumentException.class, () -> residents.save(admin,
                    new Resident(null, apartment.id(), "Resident born after move-in", LocalDate.now().minusYears(10),
                            Gender.FEMALE, null, HouseholdRelationship.OTHER, LocalDate.now().minusYears(11)), false));
            assertThrows(IllegalArgumentException.class, () -> residents.save(admin,
                    new Resident(null, inactiveApartment.id(), "Resident in inactive apartment", LocalDate.now().minusYears(10),
                            Gender.MALE, null, HouseholdRelationship.HEAD, LocalDate.now().minusYears(1)), false));
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
                delete.setLong(1, inactiveApartment.id());
                delete.executeUpdate();
            }
            try (Connection connection = database.openConnection();
                 PreparedStatement update = connection.prepareStatement(
                         "DELETE FROM apartments WHERE id IN (?, ?)")) {
                update.setLong(1, apartment.id());
                update.setLong(2, inactiveApartment.id());
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
        String email = username + "@gmail.com";
        String phone = "090" + String.valueOf(System.currentTimeMillis()).substring(7) + "1";
        String residentUsername = username + "-resident";
        String residentEmail = residentUsername + "@gmail.com";
        String residentPhone = "091" + String.valueOf(System.currentTimeMillis()).substring(7) + "1";
        AccountService accounts = new AccountService(repository);
        try {
            assertThrows(IllegalArgumentException.class, () -> accounts.create(admin, username,
                    "Tài khoản kiểm tra", Role.ACCOUNTANT, "x".repeat(73), "x".repeat(73)));
            accounts.create(admin, username, "Tài khoản kiểm tra", Role.ACCOUNTANT,
                    null, email, phone, "BlueMoon123", "BlueMoon123");
            AppUser account = auth.login(email, "BlueMoon123");
            assertNotNull(account);
            assertNotNull(auth.login(phone, "BlueMoon123"));
            assertNotNull(auth.login(username.toUpperCase(java.util.Locale.ROOT), "BlueMoon123"));
            accounts.changePassword(account, "BlueMoon123", "NewMoon123", "NewMoon123");
            assertNotNull(auth.login(email, "NewMoon123"));
            accounts.resetPassword(phone, "Recovered123", "Recovered123");
            assertNotNull(auth.login(email, "Recovered123"));
            accounts.create(admin, residentUsername, "Người dùng kiểm tra", Role.RESIDENT,
                    repository.listApartments().getFirst().id(), residentEmail, residentPhone,
                    "Resident123", "Resident123");
            AppUser resident = auth.login(residentEmail, "Resident123");
            assertNotNull(resident);
            assertEquals(Role.RESIDENT, resident.role());
            assertEquals(repository.listApartments().getFirst().id(), resident.apartmentId());
            for (int attempt = 0; attempt < 4; attempt++) assertNull(auth.login(username, "wrong-password"));
            assertThrows(AccountLockedException.class, () -> auth.login(username, "wrong-password"));
            assertThrows(AccountLockedException.class, () -> auth.login(username, "NewMoon123"));
        } finally {
            try (Connection connection = database.openConnection();
                 PreparedStatement delete = connection.prepareStatement("DELETE FROM app_users WHERE username IN (?, ?)")) {
                delete.setString(1, username);
                delete.setString(2, residentUsername);
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

    private static void setFeeActive(Database database, String code, boolean active) throws SQLException {
        try (Connection connection = database.openConnection();
             PreparedStatement update = connection.prepareStatement("UPDATE fee_types SET active = ? WHERE code = ?")) {
            update.setBoolean(1, active);
            update.setString(2, code);
            update.executeUpdate();
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

    private static int residentCount(AppRepository repository) throws SQLException {
        int count = 0;
        for (Apartment apartment : repository.listApartments()) {
            count += repository.listResidents(apartment.id()).size();
        }
        return count;
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
