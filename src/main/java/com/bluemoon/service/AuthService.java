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
        String hash = repository.findPasswordHash(username.trim());
        if (hash == null || !BCrypt.checkpw(password, hash)) return null;
        return repository.findUser(username.trim());
    }
}
