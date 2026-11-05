package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Gender;
import com.bluemoon.model.Domain.HouseholdRelationship;
import com.bluemoon.model.Domain.Resident;
import com.bluemoon.model.Domain.ResidentDirectoryEntry;
import com.bluemoon.model.Domain.Role;

import java.sql.SQLException;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;

public final class ResidentService {
    private final AppRepository repository;

    public ResidentService(AppRepository repository) {
        this.repository = repository;
    }

    public List<ResidentDirectoryEntry> search(AppUser user, String term) throws SQLException {
        AccessRules.requireSignedIn(user);
        String query = normalize(term);
        List<ResidentDirectoryEntry> directory = visibleDirectory(user);
        if (query.isEmpty()) return directory;
        Integer floor = query.matches("\\d{1,2}") ? Integer.valueOf(query) : null;
        return directory.stream().filter(entry -> {
            var apartment = entry.apartment();
            var resident = entry.resident();
            return (normalize(apartment.code()).startsWith(query)
                    || normalize(apartment.code().substring(1)).startsWith(query))
                    || normalize(apartment.householdCode()).contains(query)
                    || normalize(apartment.householdName()).contains(query)
                    || apartment.contactPhone().contains(query)
                    || (floor != null && apartment.floor() == floor)
                    || (resident != null && (normalize(resident.fullName()).contains(query)
                    || (resident.nationalId() != null && resident.nationalId().equals(term.trim()))));
        }).toList();
    }

    public List<Resident> list(AppUser user, long apartmentId) throws SQLException {
        AccessRules.requireSignedIn(user);
        if (user.role() == Role.RESIDENT && !Long.valueOf(apartmentId).equals(user.apartmentId())) {
            throw new SecurityException("Bạn chỉ được xem nhân khẩu trong căn hộ của mình.");
        }
        return repository.listResidents(apartmentId);
    }

    private List<ResidentDirectoryEntry> visibleDirectory(AppUser user) throws SQLException {
        List<ResidentDirectoryEntry> directory = repository.listResidentDirectory();
        if (user.role() != Role.RESIDENT) return directory;
        if (user.apartmentId() == null) return List.of();
        return directory.stream().filter(entry -> entry.apartment().id().equals(user.apartmentId())).toList();
    }

    public void save(AppUser user, Resident resident, boolean replaceHead) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        String fullName = resident.fullName() == null ? "" : resident.fullName().trim().replaceAll("\\s+", " ");
        if (!fullName.matches("[\\p{L} ]{2,50}")) {
            throw new IllegalArgumentException("Họ tên cần có từ 2 đến 50 ký tự chữ và khoảng trắng.");
        }
        LocalDate today = LocalDate.now();
        if (resident.dateOfBirth() == null || !resident.dateOfBirth().isBefore(today)) {
            throw new IllegalArgumentException("Ngày sinh phải trước ngày hiện tại.");
        }
        if (resident.gender() == null) throw new IllegalArgumentException("Chọn giới tính.");
        String nationalId = resident.nationalId() == null || resident.nationalId().isBlank()
                ? null : resident.nationalId().trim();
        if (nationalId != null && !nationalId.matches("(?:[0-9]{9}|[0-9]{12})")) {
            throw new IllegalArgumentException("Số CCCD hoặc CMND gồm 9 hoặc 12 chữ số.");
        }
        if (Period.between(resident.dateOfBirth(), today).getYears() >= 14 && nationalId == null) {
            throw new IllegalArgumentException("Cư dân từ 14 tuổi trở lên cần có số CCCD hoặc CMND.");
        }
        if (resident.relationship() == null) throw new IllegalArgumentException("Chọn quan hệ với chủ hộ.");
        if (resident.moveInDate() == null || resident.moveInDate().isAfter(today)) {
            throw new IllegalArgumentException("Ngày chuyển đến không được lớn hơn ngày hiện tại.");
        }

        if (resident.moveInDate().isBefore(resident.dateOfBirth())) {
            throw new IllegalArgumentException("Ngày chuyển đến không được trước ngày sinh.");
        }
        var apartment = repository.listApartments().stream()
                .filter(value -> value.id() == resident.apartmentId())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy căn hộ."));
        if (resident.id() == null && !apartment.active()) {
            throw new IllegalArgumentException("Không thể thêm nhân khẩu vào căn hộ đã ngừng hoạt động.");
        }

        List<Resident> household = repository.listResidents(resident.apartmentId());
        Resident current = household.stream().filter(value -> value.id().equals(resident.id())).findFirst().orElse(null);
        if (resident.id() != null && current == null) throw new IllegalArgumentException("Không tìm thấy nhân khẩu cần cập nhật.");
        Resident otherHead = household.stream().filter(value -> value.relationship() == HouseholdRelationship.HEAD
                && (resident.id() == null || !value.id().equals(resident.id()))).findFirst().orElse(null);
        if (resident.relationship() == HouseholdRelationship.HEAD && otherHead != null && !replaceHead) {
            throw new HouseholdHeadExistsException(otherHead.fullName());
        }
        if (resident.relationship() != HouseholdRelationship.HEAD && otherHead == null) {
            throw new IllegalArgumentException("Hộ đang có cư dân phải có đúng một chủ hộ.");
        }

        Resident normalized = new Resident(resident.id(), resident.apartmentId(), fullName, resident.dateOfBirth(),
                resident.gender(), nationalId, resident.relationship(), resident.moveInDate());
        try {
            repository.saveResident(normalized, replaceHead);
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState())) {
                throw new IllegalArgumentException("Số CCCD hoặc quan hệ chủ hộ đã tồn tại trong hệ thống.", exception);
            }
            throw exception;
        }
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String folded = value.trim().toLowerCase(Locale.ROOT).replace('đ', 'd');
        return Normalizer.normalize(folded, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
    }
}
