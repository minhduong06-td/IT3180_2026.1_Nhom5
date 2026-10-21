package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.ChargeSummary;
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
