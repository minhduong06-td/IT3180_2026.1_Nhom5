package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Role;
import com.bluemoon.model.Domain.Vehicle;
import com.bluemoon.model.Domain.VehicleType;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public final class VehicleService {
    private final AppRepository repository;

    public VehicleService(AppRepository repository) {
        this.repository = repository;
    }

    public List<Vehicle> list(AppUser user) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        return repository.listVehicles();
    }

    public void register(AppUser user, long apartmentId, VehicleType type, String plate) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        if (apartmentId <= 0 || type == null) throw new IllegalArgumentException("Chọn căn hộ và loại phương tiện.");
        String compactPlate = plate == null ? "" : plate.trim().toUpperCase(Locale.ROOT).replaceAll("[ .-]", "");
        if (!compactPlate.matches("[0-9]{2}[A-Z]{1,2}[0-9]{4,5}")) {
            throw new IllegalArgumentException("Biển số cần có dạng 30A-12345 hoặc 30AB-12345.");
        }
        int prefixLength = compactPlate.charAt(3) >= 'A' && compactPlate.charAt(3) <= 'Z' ? 4 : 3;
        String normalizedPlate = compactPlate.substring(0, prefixLength) + "-" + compactPlate.substring(prefixLength);
        try {
            repository.registerVehicle(apartmentId, type, normalizedPlate, user.id());
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState())) {
                throw new IllegalArgumentException("Biển số đang được đăng ký cho một phương tiện khác.", exception);
            }
            throw exception;
        }
    }

    public void deactivate(AppUser user, long vehicleId) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        if (vehicleId <= 0) throw new IllegalArgumentException("Chọn phương tiện cần ngừng đăng ký.");
        repository.deactivateVehicle(vehicleId);
    }
}
