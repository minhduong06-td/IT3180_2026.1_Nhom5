package com.bluemoon.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContactInfoTest {
    @Test
    void normalizesGmailAddressForLogin() {
        assertEquals("user@gmail.com", ContactInfo.normalizeEmail("  User@GMAIL.com "));
    }

    @Test
    void normalizesVietnamesePhoneWithCountryCode() {
        assertEquals("0903123401", ContactInfo.normalizePhone("+84 903 123 401"));
    }

    @Test
    void requiresAtLeastOneContactForAccount() {
        assertThrows(IllegalArgumentException.class, () -> ContactInfo.normalizeContacts("", ""));
    }

    @Test
    void recoveryIdentifierMustBeEmailOrPhone() {
        assertThrows(IllegalArgumentException.class, () -> ContactInfo.normalizeRecoveryIdentifier("admin"));
    }
}
