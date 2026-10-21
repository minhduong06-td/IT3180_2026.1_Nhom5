package com.bluemoon.service;

import com.bluemoon.dao.AppRepository;
import com.bluemoon.model.Domain.AppUser;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public final class AuthService {
    private final AppRepository repository;

    public AuthService(AppRepository repository) {
        this.repository = repository;
    }

    public AppUser login(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) return null;
        var credential = repository.findLoginCredential(username.trim());
        if (credential == null) return null;
        if (credential.loginLocked()) throw new AccountLockedException();
        if (!BCrypt.checkpw(password, credential.passwordHash())) {
            if (repository.recordLoginFailure(credential.user().id())) throw new AccountLockedException();
            return null;
        }
        repository.resetLoginFailures(credential.user().id());
        return credential.user();
    }
}
