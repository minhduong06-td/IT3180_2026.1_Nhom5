package com.bluemoon.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleLabelsTest {
    @Test
    void usesProductLabelsForAllApplicationRoles() {
        assertEquals("Ban quản trị", RoleLabels.displayName(Domain.Role.ADMIN));
        assertEquals("Kế toán", RoleLabels.displayName(Domain.Role.ACCOUNTANT));
        assertEquals("Người dùng", RoleLabels.displayName(Domain.Role.RESIDENT));
    }

    @Test
    void residentCannotSeeAdministrativeScreens() {
        assertTrue(RoleLabels.canSeeResidentData(Domain.Role.RESIDENT));
        assertFalse(RoleLabels.canManageAccounts(Domain.Role.RESIDENT));
        assertFalse(RoleLabels.canManageFees(Domain.Role.RESIDENT));
        assertTrue(RoleLabels.canManageAccounts(Domain.Role.ADMIN));
        assertTrue(RoleLabels.canManageFees(Domain.Role.ADMIN));
    }
}
