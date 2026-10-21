package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.Apartment;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Role;

import java.sql.SQLException;
import java.util.List;

public final class ApartmentService {
    private final AppRepository repository;

    public ApartmentService(AppRepository repository) {
        this.repository = repository;
    }

    public List<Apartment> list(AppUser user) throws SQLException {
        AccessRules.requireSignedIn(user);
        return repository.listApartments();
    }

    public void save(AppUser user, Apartment apartment) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        if (apartment.code() == null || !apartment.code().trim().matches("[A-Za-z0-9-]{1,20}")) {
            throw new IllegalArgumentException("Mã căn hộ chỉ gồm chữ, số hoặc dấu gạch ngang.");
        }
        if (apartment.areaM2() == null || apartment.areaM2().signum() <= 0 || apartment.areaM2().scale() > 2) {
            throw new IllegalArgumentException("Diện tích phải lớn hơn 0 và có tối đa 2 chữ số thập phân.");
        }
        if (blank(apartment.householdName()) || blank(apartment.contactPhone())) {
            throw new IllegalArgumentException("Cần nhập tên chủ hộ và số điện thoại liên hệ.");
        }
        repository.saveApartment(new Apartment(apartment.id(), apartment.code().trim().toUpperCase(),
                apartment.areaM2(), apartment.householdName().trim(), apartment.contactPhone().trim(), apartment.active()));
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
