package com.bluemoon.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class Domain {
    private Domain() {}

    public enum Role { ADMIN, ACCOUNTANT }
    public enum CalculationBasis { PER_M2, FIXED }
    public enum PaymentMethod { CASH, BANK_TRANSFER }
    public enum ChargeStatus { UNPAID, PARTIALLY_PAID, PAID }

    public record AppUser(long id, String username, String displayName, Role role) {}

    public record UserAccount(long id, String username, String displayName, Role role,
                              boolean active, boolean loginLocked) {}

    public record LoginCredential(AppUser user, String passwordHash, boolean loginLocked) {}

    public record Apartment(Long id, String code, BigDecimal areaM2, String householdName,
                            String contactPhone, boolean active) {}

    public record FeeType(long id, String code, String name, boolean required,
                          CalculationBasis basis, BigDecimal unitPrice,
                          LocalDate effectiveFrom, boolean active) {}

    public record BillingPeriod(long id, int year, int month, LocalDate issueDate,
                                LocalDate dueDate, int chargeCount, BigDecimal totalAmount) {
        public String label() {
            return "%02d/%d".formatted(month, year);
        }
    }

    public record ChargeSummary(long id, long periodId, String apartmentCode, String householdName,
                                String feeName, String period, CalculationBasis basis,
                                BigDecimal quantity, BigDecimal unitPrice, BigDecimal amount,
                                BigDecimal paid, BigDecimal balance, ChargeStatus status,
                                LocalDate dueDate) {}

    public record PaymentRecord(long id, String apartmentCode, String householdName,
                                String feeName, String period, LocalDateTime paidAt,
                                BigDecimal amount, PaymentMethod method, String reference,
                                String note, String recordedBy) {}

    public record DashboardSummary(String period, BigDecimal totalAmount, BigDecimal paidAmount,
                                   BigDecimal balance, int apartmentCount, int paidCharges,
                                   int openCharges) {}
}
