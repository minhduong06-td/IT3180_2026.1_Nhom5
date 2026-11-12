package com.bluemoon.service;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.PaymentMethod;
import com.bluemoon.model.Domain.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthorizationTest {
    private final AppUser admin = new AppUser(1, "admin", "Admin", Role.ADMIN);
    private final AppUser accountant = new AppUser(2, "accountant", "Accountant", Role.ACCOUNTANT);
    private final AppUser resident = new AppUser(3, "resident", "Người dùng", Role.RESIDENT);

    @Test
    void accountantCannotChangeApartmentOrIssuePeriods() {
        assertThrows(SecurityException.class, () -> new ApartmentService(null).save(accountant, null));
        assertThrows(SecurityException.class, () -> new BillingService(null).issue(accountant, 2026, 10,
                java.time.LocalDate.of(2026, 10, 1), java.time.LocalDate.of(2026, 10, 31)));
    }

    @Test
    void administratorCannotRecordPayments() {
        assertThrows(SecurityException.class, () -> new CollectionService(null).recordPayment(admin, 1,
                java.math.BigDecimal.ONE, "Admin", java.time.LocalDate.now(), PaymentMethod.CASH, null, null));
    }

    @Test
    void accountantCannotManageUtilityBillsOrResidentRequests() {
        assertThrows(SecurityException.class, () -> new UtilityService(null).list(accountant));
        assertThrows(SecurityException.class, () -> new ResidentRequestService(null).list(accountant));
        assertThrows(SecurityException.class, () -> new ResidentRequestService(null).save(accountant, null));
        assertThrows(SecurityException.class, () -> new VehicleService(null).list(accountant));
    }

    @Test
    void residentCannotManageAccountsOrIssuePeriods() {
        assertThrows(SecurityException.class, () -> new AccountService(null).list(resident));
        assertThrows(SecurityException.class, () -> new BillingService(null).issue(resident, 2026, 10,
                java.time.LocalDate.of(2026, 10, 1), java.time.LocalDate.of(2026, 10, 31)));
        assertThrows(SecurityException.class, () -> new FeeService(null).list(resident));
    }
}
