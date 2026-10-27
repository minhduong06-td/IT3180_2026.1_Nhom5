package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Role;
import com.bluemoon.model.Domain.UtilityBill;
import com.bluemoon.model.Domain.UtilityBillInput;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public final class UtilityService {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("9999999999999");
    private final AppRepository repository;

    public UtilityService(AppRepository repository) {
        this.repository = repository;
    }

    public List<UtilityBill> list(AppUser user) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        return repository.listUtilityBills();
    }

    public void save(AppUser user, UtilityBillInput bill) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        if (bill == null || bill.year() < 2000 || bill.year() > 2200 || bill.month() < 1 || bill.month() > 12
                || bill.apartmentId() <= 0 || bill.type() == null) {
            throw new IllegalArgumentException("Chọn tháng, căn hộ và loại hóa đơn hợp lệ.");
        }
        if (bill.amount() == null || bill.amount().signum() <= 0
                || bill.amount().stripTrailingZeros().scale() > 0 || bill.amount().compareTo(MAX_AMOUNT) > 0) {
            throw new IllegalArgumentException("Số tiền phải là số nguyên VND dương, tối đa 13 chữ số.");
        }
        String provider = bill.provider() == null ? "" : bill.provider().trim();
        String reference = bill.reference() == null || bill.reference().isBlank() ? null : bill.reference().trim();
        if (provider.length() < 2 || provider.length() > 100 || (reference != null && reference.length() > 120)) {
            throw new IllegalArgumentException("Nhập tên nhà cung cấp từ 2 đến 100 ký tự; mã đối chiếu tối đa 120 ký tự.");
        }
        repository.saveUtilityBill(new UtilityBillInput(bill.id(), bill.year(), bill.month(), bill.apartmentId(),
                bill.type(), bill.amount(), provider, reference), user.id());
    }

    public void delete(AppUser user, long billId) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        if (billId <= 0) throw new IllegalArgumentException("Chọn hóa đơn cần xóa.");
        repository.deleteUtilityBill(billId);
    }
}
