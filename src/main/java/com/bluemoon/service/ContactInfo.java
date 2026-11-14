package com.bluemoon.service;

import java.util.Locale;

/** Normalizes the contact values used as account login and recovery identifiers. */
public record ContactInfo(String email, String phone) {
    private static final int MAX_EMAIL_LENGTH = 254;

    public static ContactInfo normalizeContacts(String email, String phone) {
        String normalizedEmail = optionalEmail(email);
        String normalizedPhone = optionalPhone(phone);
        if (normalizedEmail == null && normalizedPhone == null) {
            throw new IllegalArgumentException("Nhập email hoặc số điện thoại cho tài khoản.");
        }
        return new ContactInfo(normalizedEmail, normalizedPhone);
    }

    public static String normalizeEmail(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty() || normalized.length() > MAX_EMAIL_LENGTH
                || !normalized.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email chưa đúng định dạng.");
        }
        return normalized;
    }

    public static String normalizePhone(String value) {
        String normalized = value == null ? "" : value.trim().replaceAll("[\\s().-]", "");
        if (normalized.startsWith("+84")) normalized = "0" + normalized.substring(3);
        else if (normalized.startsWith("84") && normalized.length() == 11) normalized = "0" + normalized.substring(2);
        if (!normalized.matches("0\\d{9}")) {
            throw new IllegalArgumentException("Số điện thoại chưa đúng định dạng.");
        }
        return normalized;
    }

    public static String normalizeRecoveryIdentifier(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.contains("@")) return normalizeEmail(normalized);
        if (normalized.matches("[+0-9\\s().-]+")) return normalizePhone(normalized);
        throw new IllegalArgumentException("Nhập email hoặc số điện thoại đã đăng ký.");
    }

    public static String normalizeLoginIdentifier(String value) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.contains("@")) return normalizeEmail(normalized);
        if (normalized.matches("[+0-9\\s().-]+")) {
            try {
                return normalizePhone(normalized);
            } catch (IllegalArgumentException exception) {
                if (normalized.startsWith("+") || normalized.length() >= 9) throw exception;
            }
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static String optionalEmail(String value) {
        return value == null || value.isBlank() ? null : normalizeEmail(value);
    }

    private static String optionalPhone(String value) {
        return value == null || value.isBlank() ? null : normalizePhone(value);
    }
}
