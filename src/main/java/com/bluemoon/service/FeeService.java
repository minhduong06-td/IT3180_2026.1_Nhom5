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
        create(user, code, name, true, basis, price, effectiveFrom);
    }

    public void create(AppUser user, String code, String name, boolean required, CalculationBasis basis,
                       BigDecimal price, LocalDate effectiveFrom) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        if (code == null || !code.trim().matches("[A-Za-z0-9_-]{1,20}")) {
            throw new IllegalArgumentException("Mã khoản thu chỉ gồm chữ, số, gạch ngang hoặc gạch dưới.");
        }
        if (name == null || name.trim().length() < 3 || name.trim().length() > 100 || basis == null) {
            throw new IllegalArgumentException("Cần nhập tên và cách tính khoản thu.");
        }
        if (basis == CalculationBasis.EXTERNAL_BILL) {
            throw new IllegalArgumentException("Hóa đơn nhà cung cấp cần được nhập trong mục Điện, nước, internet.");
        }
        if (required) {
            if (basis == CalculationBasis.VOLUNTARY) throw new IllegalArgumentException("Khoản bắt buộc cần cách tính cụ thể.");
            validateRate(price, effectiveFrom);
        } else if (basis != CalculationBasis.VOLUNTARY) {
            throw new IllegalArgumentException("Khoản đóng góp tự nguyện không áp dụng đơn giá cố định.");
        }
        repository.createFeeType(code.trim().toUpperCase(java.util.Locale.ROOT), name.trim(), required,
                basis, price, effectiveFrom, user.id());
    }

    public void addRate(AppUser user, long feeId, BigDecimal price, LocalDate effectiveFrom) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        validateRate(price, effectiveFrom);
        FeeType fee = repository.listFeeTypes().stream().filter(item -> item.id() == feeId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoản thu."));
        if (!fee.required() || !fee.active() || fee.basis() == CalculationBasis.VOLUNTARY) {
            throw new IllegalArgumentException("Chỉ khoản thu bắt buộc đang áp dụng mới có thể cập nhật đơn giá.");
        }
        repository.addFeeRate(feeId, price, effectiveFrom, user.id());
    }

    public void setActive(AppUser user, long feeId, boolean active) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        FeeType fee = repository.listFeeTypes().stream().filter(item -> item.id() == feeId).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoản thu."));
        if (fee.active() != active) repository.setFeeActive(feeId, active);
    }

    private static void validateRate(BigDecimal price, LocalDate effectiveFrom) {
        if (price == null || price.signum() <= 0 || price.scale() > 2) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn 0 và có tối đa 2 chữ số thập phân.");
        }
        if (effectiveFrom == null) throw new IllegalArgumentException("Cần chọn ngày bắt đầu hiệu lực.");
    }
}
