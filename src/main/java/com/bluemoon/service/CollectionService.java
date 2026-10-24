package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.ChargeSummary;
import com.bluemoon.model.Domain.ChargeStatus;
import com.bluemoon.model.Domain.HouseholdDebt;
import com.bluemoon.model.Domain.PaymentMethod;
import com.bluemoon.model.Domain.PaymentRecord;
import com.bluemoon.model.Domain.Role;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public final class CollectionService {
    private final AppRepository repository;

    public CollectionService(AppRepository repository) {
        this.repository = repository;
    }

    public List<ChargeSummary> charges(AppUser user, Long periodId, String search) throws SQLException {
        AccessRules.requireSignedIn(user);
        return repository.listCharges(periodId, search);
    }

    public List<HouseholdDebt> debts(AppUser user, long periodId, Integer floorFrom, Integer floorTo,
                                     ChargeStatus status) throws SQLException {
        AccessRules.requireSignedIn(user);
        if (periodId <= 0) throw new IllegalArgumentException("Chọn kỳ thu cần xem.");
        if ((floorFrom != null && (floorFrom < 1 || floorFrom > 30))
                || (floorTo != null && (floorTo < 1 || floorTo > 30))
                || (floorFrom != null && floorTo != null && floorFrom > floorTo)) {
            throw new IllegalArgumentException("Khoảng tầng phải nằm trong tầng 1 đến 30 và có thứ tự hợp lệ.");
        }
        return repository.listHouseholdDebts(periodId).stream()
                .filter(debt -> floorFrom == null || debt.floor() >= floorFrom)
                .filter(debt -> floorTo == null || debt.floor() <= floorTo)
                .filter(debt -> status == null || debt.status() == status)
                .toList();
    }

    public void recordPayment(AppUser user, long chargeId, BigDecimal amount, PaymentMethod method,
                              String reference, String note) throws SQLException {
        AccessRules.requireRole(user, Role.ACCOUNTANT);
        if (method == null) throw new IllegalArgumentException("Cần chọn hình thức thanh toán.");
        repository.recordPayment(chargeId, amount, method, reference, note, user.id());
    }

    public List<PaymentRecord> payments(AppUser user, String search) throws SQLException {
        AccessRules.requireSignedIn(user);
        return repository.listPayments(search);
    }
}
