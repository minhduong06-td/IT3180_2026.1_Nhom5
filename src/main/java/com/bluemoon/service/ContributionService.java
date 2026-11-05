package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.CalculationBasis;
import com.bluemoon.model.Domain.Contribution;
import com.bluemoon.model.Domain.ContributionRecord;
import com.bluemoon.model.Domain.FeeType;
import com.bluemoon.model.Domain.PaymentMethod;
import com.bluemoon.model.Domain.Role;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class ContributionService {
    private final AppRepository repository;

    public ContributionService(AppRepository repository) {
        this.repository = repository;
    }

    public List<ContributionRecord> list(AppUser user) throws SQLException {
        AccessRules.requireSignedIn(user);
        String apartmentCode = residentApartmentCode(user);
        return repository.listContributions().stream()
                .filter(record -> apartmentCode == null || apartmentCode.equals(record.apartmentCode()))
                .toList();
    }

    public void record(AppUser user, Contribution contribution) throws SQLException {
        AccessRules.requireAnyRole(user, Role.ADMIN, Role.ACCOUNTANT);
        if (contribution == null || contribution.amount() == null || contribution.amount().signum() <= 0
                || contribution.amount().stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Số tiền đóng góp phải là số nguyên VND dương.");
        }
        if (contribution.receivedOn() == null || contribution.receivedOn().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày nhận không được lớn hơn ngày hiện tại.");
        }
        if (contribution.method() == null) throw new IllegalArgumentException("Chọn hình thức nhận tiền.");
        if (contribution.reference() != null && contribution.reference().length() > 120) {
            throw new IllegalArgumentException("Mã giao dịch tối đa 120 ký tự.");
        }
        if (contribution.note() != null && contribution.note().length() > 500) {
            throw new IllegalArgumentException("Ghi chú tối đa 500 ký tự.");
        }
        FeeType fee = repository.listFeeTypes().stream().filter(item -> item.id() == contribution.feeTypeId())
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoản đóng góp."));
        if (fee.required() || !fee.active() || fee.basis() != CalculationBasis.VOLUNTARY) {
            throw new IllegalArgumentException("Chọn khoản đóng góp đang áp dụng.");
        }
        if (repository.listApartments().stream().noneMatch(apartment -> apartment.id() == contribution.apartmentId()
                && apartment.active())) {
            throw new IllegalArgumentException("Chọn căn hộ đang hoạt động.");
        }
        repository.recordContribution(contribution, user.id());
    }

    private String residentApartmentCode(AppUser user) throws SQLException {
        if (user.role() != Role.RESIDENT) return null;
        if (user.apartmentId() == null) return "";
        return repository.listApartments().stream()
                .filter(apartment -> apartment.id().equals(user.apartmentId()))
                .map(apartment -> apartment.code())
                .findFirst().orElse("");
    }
}
