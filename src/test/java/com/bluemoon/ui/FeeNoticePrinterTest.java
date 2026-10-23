package com.bluemoon.ui;

import com.bluemoon.model.Domain.CalculationBasis;
import com.bluemoon.model.Domain.ChargeStatus;
import com.bluemoon.model.Domain.ChargeSummary;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FeeNoticePrinterTest {
    @Test
    void formatsApartmentAndPeriodForTransferReference() {
        ChargeSummary charge = new ChargeSummary(1, 2, "P1204", "Nguyễn Văn An", "Phí quản lý",
                "11/2026", CalculationBasis.PER_M2, new BigDecimal("80"), new BigDecimal("7000"),
                new BigDecimal("560000"), BigDecimal.ZERO, new BigDecimal("560000"), ChargeStatus.UNPAID,
                LocalDate.of(2026, 12, 15));

        assertEquals("P1204 Dong phi thang 11/2026", FeeNoticePrinter.transferDescription(charge));
    }
}
