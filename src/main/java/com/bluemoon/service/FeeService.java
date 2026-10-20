package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.CalculationBasis;
import com.bluemoon.model.Domain.FeeType;
import com.bluemoon.model.Domain.Role;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class FeeService {
    private final AppRepository repository;

    public FeeService(AppRepository repository) {
        this.repository = repository;
    }

    public List<FeeType> list(AppUser user) throws SQLException {
        AccessRules.requireSignedIn(user);
        return repository.listFeeTypes();
    }

    public void create(AppUser user, String code, String name, CalculationBasis basis,
                       BigDecimal price, LocalDate effectiveFrom) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        validateRate(price, effectiveFrom);
        if (code == null || !code.trim().matches("[A-Za-z0-9_-]{1,32}")) {
            throw new IllegalArgumentException("Mã khoản thu chỉ gồm chữ, số, gạch ngang hoặc gạch dưới.");
        }
        if (name == null || name.isBlank() || basis == null) {
            throw new IllegalArgumentException("Cần nhập tên và cách tính khoản thu.");
        }
        repository.createFeeType(code.trim().toUpperCase(), name.trim(), true, basis, price, effectiveFrom, user.id());
    }

    public void addRate(AppUser user, long feeId, BigDecimal price, LocalDate effectiveFrom) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        validateRate(price, effectiveFrom);
        repository.addFeeRate(feeId, price, effectiveFrom, user.id());
    }

    private static void validateRate(BigDecimal price, LocalDate effectiveFrom) {
        if (price == null || price.signum() <= 0 || price.scale() > 2) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn 0 và có tối đa 2 chữ số thập phân.");
        }
        if (effectiveFrom == null) throw new IllegalArgumentException("Cần chọn ngày bắt đầu hiệu lực.");
    }
}
