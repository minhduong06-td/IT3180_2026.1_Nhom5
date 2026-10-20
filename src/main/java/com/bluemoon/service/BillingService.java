package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.BillingPeriod;
import com.bluemoon.model.Domain.Role;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class BillingService {
    private final AppRepository repository;

    public BillingService(AppRepository repository) {
        this.repository = repository;
    }

    public List<BillingPeriod> list(AppUser user) throws SQLException {
        AccessRules.requireSignedIn(user);
        return repository.listBillingPeriods();
    }

    public BillingPeriod issue(AppUser user, int year, int month, LocalDate issueDate, LocalDate dueDate) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        if (year < 2000 || year > 2200 || month < 1 || month > 12) {
            throw new IllegalArgumentException("Tháng hoặc năm không hợp lệ.");
        }
        if (issueDate == null || dueDate == null || dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Hạn nộp phải cùng ngày hoặc sau ngày phát hành.");
        }
        if (issueDate.getYear() != year || issueDate.getMonthValue() != month) {
            throw new IllegalArgumentException("Ngày phát hành phải thuộc tháng của kỳ thu.");
        }
        return repository.issuePeriod(year, month, issueDate, dueDate, user.id());
    }
}
