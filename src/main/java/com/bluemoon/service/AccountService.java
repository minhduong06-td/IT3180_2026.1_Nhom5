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
        try {
            return repository.createInitialAdmin(normalizedUsername, BCrypt.hashpw(password, BCrypt.gensalt(10)),
                    displayName.trim());
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState())) {
                throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng.", exception);
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
        AccessRules.requireRole(user, Role.ADMIN);
        String normalizedUsername = normalizeUsername(username);
        validateDisplayName(displayName);
        validatePassword(password, confirmation);
        if (role == null) throw new IllegalArgumentException("Chọn vai trò cho tài khoản.");
        try {
            repository.createUser(normalizedUsername, BCrypt.hashpw(password, BCrypt.gensalt(10)), displayName.trim(), role);
        } catch (SQLException exception) {
            if ("23000".equals(exception.getSQLState())) {
                throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng.", exception);
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

    private static void validatePassword(String password, String confirmation) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu cần có ít nhất 8 ký tự.");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Mật khẩu không được vượt quá 72 byte.");
        }
        if (!password.equals(confirmation)) {
            throw new IllegalArgumentException("Mật khẩu xác nhận chưa khớp.");
        }
    }
}
