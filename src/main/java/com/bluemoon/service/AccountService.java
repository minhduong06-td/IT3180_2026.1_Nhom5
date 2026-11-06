package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Role;
import com.bluemoon.model.Domain.UserAccount;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public final class AccountService {
    private final AppRepository repository;

    public AccountService(AppRepository repository) {
        this.repository = repository;
    }

    public AppUser createInitialAdmin(String username, String displayName, String password,
                                      String confirmation) throws SQLException {
        String normalizedUsername = normalizeUsername(username);
        validateDisplayName(displayName);
        validatePassword(password, confirmation);
        return repository.createInitialAdmin(normalizedUsername, BCrypt.hashpw(password, BCrypt.gensalt(10)),
                displayName.trim());
    }

    public AppUser createInitialAdmin(String username, String displayName, String email, String phone,
                                      String password, String confirmation) throws SQLException {
        String normalizedUsername = normalizeUsername(username);
        ContactInfo contacts = ContactInfo.normalizeContacts(email, phone);
        validateDisplayName(displayName);
        validatePassword(password, confirmation);
        try {
            return repository.createInitialAdmin(normalizedUsername, BCrypt.hashpw(password, BCrypt.gensalt(10)),
                    displayName.trim(), contacts.email(), contacts.phone());
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState())) {
                throw new IllegalArgumentException("Tên tài khoản, email hoặc số điện thoại đã được sử dụng.", exception);
            }
            throw exception;
        }
    }

    public List<UserAccount> list(AppUser user) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        return repository.listUserAccounts();
    }

    public void create(AppUser user, String username, String displayName, Role role,
                       String password, String confirmation) throws SQLException {
        createInternal(user, username, displayName, role, null, null, null,
                password, confirmation, false);
    }

    public void create(AppUser user, String username, String displayName, Role role, Long apartmentId,
                       String password, String confirmation) throws SQLException {
        createInternal(user, username, displayName, role, apartmentId, null, null,
                password, confirmation, false);
    }

    public void create(AppUser user, String username, String displayName, Role role, Long apartmentId,
                       String email, String phone, String password, String confirmation) throws SQLException {
        createInternal(user, username, displayName, role, apartmentId, email, phone,
                password, confirmation, true);
    }

    private void createInternal(AppUser user, String username, String displayName, Role role, Long apartmentId,
                                String email, String phone, String password, String confirmation,
                                boolean requireContact) throws SQLException {
        AccessRules.requireRole(user, Role.ADMIN);
        String normalizedUsername = normalizeUsername(username);
        ContactInfo contacts = requireContact ? ContactInfo.normalizeContacts(email, phone)
                : new ContactInfo(null, null);
        validateDisplayName(displayName);
        validatePassword(password, confirmation);
        if (role == null) throw new IllegalArgumentException("Chọn vai trò cho tài khoản.");
        if (role == Role.RESIDENT && apartmentId == null) {
            throw new IllegalArgumentException("Tài khoản người dùng phải gắn với một căn hộ.");
        }
        if (role != Role.RESIDENT) apartmentId = null;
        try {
            repository.createUser(normalizedUsername, BCrypt.hashpw(password, BCrypt.gensalt(10)), displayName.trim(), role,
                    apartmentId, contacts.email(), contacts.phone());
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState())) {
                throw new IllegalArgumentException("Tên tài khoản, email hoặc số điện thoại đã được sử dụng.", exception);
            }
            throw exception;
        }
    }

    private static String normalizeUsername(String username) {
        String normalized = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z0-9._-]{3,64}")) {
            throw new IllegalArgumentException("Tên đăng nhập gồm 3 đến 64 ký tự chữ thường, số, dấu chấm, gạch ngang hoặc gạch dưới.");
        }
        return normalized;
    }

    private static void validateDisplayName(String displayName) {
        if (displayName == null || displayName.trim().length() < 2 || displayName.trim().length() > 120) {
            throw new IllegalArgumentException("Tên hiển thị cần có từ 2 đến 120 ký tự.");
        }
    }

    public void changePassword(AppUser user, String currentPassword, String newPassword,
                               String confirmation) throws SQLException {
        AccessRules.requireSignedIn(user);
        String hash = repository.findPasswordHash(user.id());
        if (hash == null || currentPassword == null || !BCrypt.checkpw(currentPassword, hash)) {
            throw new IllegalArgumentException("Mật khẩu hiện tại chưa đúng.");
        }
        validatePassword(newPassword, confirmation);
        if (BCrypt.checkpw(newPassword, hash)) {
            throw new IllegalArgumentException("Mật khẩu mới cần khác mật khẩu hiện tại.");
        }
        repository.updatePassword(user.id(), BCrypt.hashpw(newPassword, BCrypt.gensalt(10)));
    }

    public void resetPassword(String contact, String newPassword, String confirmation) throws SQLException {
        String normalizedContact = ContactInfo.normalizeRecoveryIdentifier(contact);
        var credential = repository.findLoginCredentialByContact(normalizedContact);
        if (credential == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản với email hoặc số điện thoại này.");
        }
        validatePassword(newPassword, confirmation);
        repository.updatePassword(credential.user().id(), BCrypt.hashpw(newPassword, BCrypt.gensalt(10)));
    }

    private static void validatePassword(String password, String confirmation) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu quá ngắn. Vui lòng chọn mật khẩu dài hơn.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Mật khẩu quá dài. Vui lòng chọn mật khẩu ngắn hơn.");
        }
        if (!password.equals(confirmation)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận chưa khớp.");
        }
    }
}
