package com.bluemoon.service;

import com.bluemoon.model.Domain.AppUser;
import com.bluemoon.model.Domain.Role;

import java.util.Arrays;

public final class AccessRules {
    private AccessRules() {}

    public static void requireSignedIn(AppUser user) {
        if (user == null) throw new SecurityException("Vui lòng đăng nhập để tiếp tục.");
    }

    public static void requireRole(AppUser user, Role role) {
        requireSignedIn(user);
        if (user.role() != role) throw new SecurityException("Tài khoản không có quyền thực hiện thao tác này.");
    }

    public static void requireAnyRole(AppUser user, Role... roles) {
        requireSignedIn(user);
        if (roles == null || Arrays.stream(roles).noneMatch(role -> role == user.role())) {
            throw new SecurityException("Tài khoản không có quyền thực hiện thao tác này.");
        }
    }
}
