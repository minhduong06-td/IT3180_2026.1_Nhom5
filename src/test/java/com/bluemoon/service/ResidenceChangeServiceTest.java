package com.bluemoon.service;

import com.bluemoon.model.Domain.ResidenceStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResidenceChangeServiceTest {
    @Test
    void acceptsAValidTemporaryAbsenceChange() {
        assertDoesNotThrow(() -> ResidenceChangeService.validate(
                1L, ResidenceStatus.TEMPORARY_ABSENCE,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 15),
                "Công tác"));
    }

    @Test
    void rejectsAnEndDateBeforeTheStartDate() {
        assertThrows(IllegalArgumentException.class, () -> ResidenceChangeService.validate(
                1L, ResidenceStatus.TEMPORARY_RESIDENCE,
                LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 1),
                "Thăm thân"));
    }

    @Test
    void rejectsAnEmptyReason() {
        assertThrows(IllegalArgumentException.class, () -> ResidenceChangeService.validate(
                1L, ResidenceStatus.PERMANENT_RESIDENCE,
                LocalDate.of(2026, 10, 1), null, ""));
    }
}
