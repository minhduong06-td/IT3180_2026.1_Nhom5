package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.ResidentRequest;
import com.bluemoon.model.Domain.ResidentRequestInput;
import com.bluemoon.model.Domain.RequestStatus;
import com.bluemoon.model.Domain.Role;

import java.sql.SQLException;
import java.util.List;

public final class ResidentRequestService {
    private final AppRepository repository;

    public ResidentRequestService(AppRepository repository) {
        this.repository = repository;
    }

    public List<ResidentRequest> list(AppUser user) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        return repository.listResidentRequests();
    }

    public void save(AppUser user, ResidentRequestInput request) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        if (request == null || request.apartmentId() <= 0 || request.category() == null || request.status() == null
                || request.id() != null && request.id() <= 0) {
            throw new IllegalArgumentException("Chọn căn hộ, nhóm yêu cầu và trạng thái hợp lệ.");
        }
        if (request.id() == null && request.status() != RequestStatus.RECEIVED) {
            throw new IllegalArgumentException("Yêu cầu mới phải ở trạng thái vừa tiếp nhận.");
        }
        String reporter = clean(request.reporterName());
        String subject = clean(request.subject());
        String description = clean(request.description());
        String handlingNote = clean(request.handlingNote());
        if (reporter.length() < 2 || reporter.length() > 100 || subject.length() < 3 || subject.length() > 120
                || description.isEmpty() || description.length() > 1500
                || handlingNote.length() > 1000) {
            throw new IllegalArgumentException("Kiểm tra tên người báo, tiêu đề, nội dung và ghi chú xử lý.");
        }
        repository.saveResidentRequest(new ResidentRequestInput(request.id(), request.apartmentId(), reporter,
                request.category(), subject, description, request.status(),
                handlingNote.isEmpty() ? null : handlingNote), user.id());
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
