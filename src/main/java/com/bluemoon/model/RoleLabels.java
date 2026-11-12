package com.bluemoon.model;

import com.bluemoon.model.Domain.Role;

public final class RoleLabels {
    private RoleLabels() {}

    public static String displayName(Role role) {
        return switch (role) {
            case ADMIN -> "Ban quản trị";
            case ACCOUNTANT -> "Kế toán";
            case RESIDENT -> "Người dùng";
        };
    }

    public static boolean canManageAccounts(Role role) {
        return role == Role.ADMIN;
    }

    public static boolean canManageFees(Role role) {
        return role == Role.ADMIN;
    }

    public static boolean canSeeResidentData(Role role) {
        return role != null;
    }
}
