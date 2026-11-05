package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.Apartment;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.OccupancyStatus;
import com.bluemoon.model.Domain.Role;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class ApartmentService {
    private final AppRepository repository;

    public ApartmentService(AppRepository repository) {
        this.repository = repository;
    }

    public List<Apartment> list(AppUser user) throws SQLException {
        AccessRules.requireSignedIn(user);
        List<Apartment> apartments = repository.listApartments();
        if (user.role() != Role.RESIDENT) return apartments;
        if (user.apartmentId() == null) return List.of();
        return apartments.stream().filter(apartment -> apartment.id().equals(user.apartmentId())).toList();
    }

    public void save(AppUser user, Apartment apartment) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        String code = apartment.code() == null ? "" : apartment.code().trim().toUpperCase(java.util.Locale.ROOT);
        boolean validCode = code.matches("[A-Z][0-9]{3,4}");
        boolean unchangedLegacyCode = !validCode && apartment.id() != null && repository.listApartments().stream()
                .anyMatch(existing -> existing.id().equals(apartment.id()) && existing.code().equalsIgnoreCase(code));
        if (!validCode && !unchangedLegacyCode) {
            throw new IllegalArgumentException("Mã căn hộ gồm một chữ cái và ba hoặc bốn chữ số, ví dụ P1002.");
        }
        int floor = validCode ? Integer.parseInt(code.substring(1, code.length() - 2)) : 0;
        if (validCode && (floor < 1 || floor > 30)) {
            throw new IllegalArgumentException("Tầng phải là số nguyên từ 1 đến 30.");
        }
        String householdCode = apartment.householdCode() == null ? "" : apartment.householdCode().trim().toUpperCase(java.util.Locale.ROOT);
        if (!householdCode.matches("HK-[A-Z0-9-]{1,17}")) {
            throw new IllegalArgumentException("Mã hộ khẩu gồm HK- và tối đa 17 chữ cái hoặc số.");
        }
        if (apartment.areaM2() == null || apartment.areaM2().signum() <= 0 || apartment.areaM2().scale() > 2) {
            throw new IllegalArgumentException("Diện tích phải lớn hơn 0 và có tối đa 2 chữ số thập phân.");
        }
        if (blank(apartment.householdName()) || blank(apartment.contactPhone())) {
            throw new IllegalArgumentException("Cần nhập tên chủ hộ và số điện thoại liên hệ.");
        }
        if (apartment.moveInDate() == null || apartment.moveInDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày nhận nhà không được lớn hơn ngày hiện tại.");
        }
        if (apartment.occupancyStatus() == null) throw new IllegalArgumentException("Chọn trạng thái căn hộ.");
        try {
            repository.saveApartment(new Apartment(apartment.id(), code, householdCode, apartment.areaM2(),
                    apartment.householdName().trim(), apartment.contactPhone().trim(), apartment.moveInDate(),
                    apartment.occupancyStatus(), apartment.active()));
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState())) {
                throw new IllegalArgumentException("Mã căn hộ hoặc mã hộ khẩu đã được sử dụng.", exception);
            }
            throw exception;
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
