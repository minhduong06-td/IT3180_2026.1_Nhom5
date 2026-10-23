package com.bluemoon.service;

import com.bluemoon.model.Domain.CalculationBasis;
import com.bluemoon.model.Domain.ChargeStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BillingRulesTest {
    @Test
    void calculatesAreaBasedFeeAndRoundsToWholeDong() {
        assertEquals(new BigDecimal("131250"), BillingRules.calculate(
                CalculationBasis.PER_M2, new BigDecimal("52.5"), new BigDecimal("2500")));
    }

    @Test
    void calculatesFixedFeeWithoutUsingApartmentArea() {
        assertEquals(new BigDecimal("2500"), BillingRules.calculate(
                CalculationBasis.FIXED, new BigDecimal("99"), new BigDecimal("2500")));
    }

    @Test
    void calculatesPerResidentFeesAndAllowsAnEmptyHousehold() {
        assertEquals(new BigDecimal("24000"), BillingRules.calculate(
                CalculationBasis.PER_RESIDENT, new BigDecimal("80"), new BigDecimal("6000"), 4));
        assertEquals(BigDecimal.ZERO, BillingRules.calculate(
                CalculationBasis.PER_RESIDENT, new BigDecimal("80"), new BigDecimal("6000"), 0));
        assertEquals(ChargeStatus.PAID, BillingRules.status(BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> BillingRules.calculate(
                CalculationBasis.VOLUNTARY, BigDecimal.ONE, BigDecimal.ONE, 1));
    }

    @Test
    void rejectsZeroAreaAndOverpayment() {
        assertThrows(IllegalArgumentException.class, () -> BillingRules.calculate(
                CalculationBasis.PER_M2, BigDecimal.ZERO, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> BillingRules.calculate(
                CalculationBasis.PER_M2, BigDecimal.ONE, new BigDecimal("0.01")));
        assertThrows(IllegalArgumentException.class, () -> BillingRules.validatePayment(
                new BigDecimal("101"), new BigDecimal("100")));
        assertThrows(IllegalArgumentException.class, () -> BillingRules.validatePayment(
                new BigDecimal("1.5"), new BigDecimal("100")));
    }

    @Test
    void derivesUnpaidPartialAndPaidStates() {
        assertEquals(ChargeStatus.UNPAID, BillingRules.status(new BigDecimal("100"), BigDecimal.ZERO));
        assertEquals(ChargeStatus.PARTIALLY_PAID, BillingRules.status(new BigDecimal("100"), new BigDecimal("40")));
        assertEquals(ChargeStatus.PAID, BillingRules.status(new BigDecimal("100"), new BigDecimal("100")));
    }
}
