package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.ResidenceChange;
import com.bluemoon.model.Domain.ResidenceStatus;
import com.bluemoon.model.Domain.Role;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class ResidenceChangeService {
    private final AppRepository repository;

    public ResidenceChangeService(AppRepository repository) {
        this.repository = repository;
    }

    public List<ResidenceChange> list(AppUser user) throws SQLException {
        AccessRules.requireSignedIn(user);
        return repository.listResidenceChanges();
    }

    public void record(AppUser user, long residentId, ResidenceStatus status, LocalDate startDate,
                       LocalDate endDate, String reason) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        validate(residentId, status, startDate, endDate, reason);
        repository.saveResidenceChange(residentId, status, startDate, endDate, reason.trim(), user.id());
    }

    public static void validate(long residentId, ResidenceStatus status, LocalDate startDate,
                                LocalDate endDate, String reason) {
        if (residentId <= 0) throw new IllegalArgumentException("Chọn nhân khẩu cần cập nhật.");
        if (status == null) throw new IllegalArgumentException("Chọn trạng thái cư trú.");
        if (startDate == null) throw new IllegalArgumentException("Chọn ngày bắt đầu.");
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu.");
        }
        if (reason == null || reason.trim().length() < 2 || reason.trim().length() > 500) {
            throw new IllegalArgumentException("Lý do cần có từ 2 đến 500 ký tự.");
        }
    }
}
