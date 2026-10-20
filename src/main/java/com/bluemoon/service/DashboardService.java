package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.DashboardSummary;

import java.sql.SQLException;

public final class DashboardService {
    private final AppRepository repository;

    public DashboardService(AppRepository repository) {
        this.repository = repository;
    }

    public DashboardSummary summary(AppUser user) throws SQLException {
        AccessRules.requireSignedIn(user);
        return repository.dashboard();
    }
}
