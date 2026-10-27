package com.bluemoon.service;

import com.bluemoon.model.Domain.CalculationBasis;
import com.bluemoon.model.Domain.ChargeStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BillingRules {
    private BillingRules() {}

    public static BigDecimal calculate(CalculationBasis basis, BigDecimal areaM2, BigDecimal unitPrice) {
        return calculate(basis, areaM2, unitPrice, 0);
    }

    public static BigDecimal calculate(CalculationBasis basis, BigDecimal areaM2,
                                       BigDecimal unitPrice, int residentCount) {
        if (basis == CalculationBasis.PER_MOTORCYCLE || basis == CalculationBasis.PER_CAR) {
            throw new IllegalArgumentException("Phí theo phương tiện cần số lượng xe tại thời điểm tính.");
        }
        return calculate(basis, areaM2, unitPrice, residentCount, 0, 0);
    }

    public static BigDecimal calculate(CalculationBasis basis, BigDecimal areaM2, BigDecimal unitPrice,
                                       int residentCount, int motorcycleCount, int carCount) {
        if (basis == null || basis == CalculationBasis.VOLUNTARY || areaM2 == null
                || areaM2.signum() <= 0 || unitPrice == null || unitPrice.signum() <= 0
                || residentCount < 0 || motorcycleCount < 0 || carCount < 0) {
            throw new IllegalArgumentException("Diện tích và đơn giá phải lớn hơn 0.");
        }
        BigDecimal quantity = switch (basis) {
            case PER_M2 -> areaM2;
            case PER_RESIDENT -> BigDecimal.valueOf(residentCount);
            case PER_MOTORCYCLE -> BigDecimal.valueOf(motorcycleCount);
            case PER_CAR -> BigDecimal.valueOf(carCount);
            case FIXED -> BigDecimal.ONE;
            case EXTERNAL_BILL -> BigDecimal.ONE;
            case VOLUNTARY -> throw new IllegalArgumentException("Khoản đóng góp không được tính tự động.");
        };
        BigDecimal amount = quantity.multiply(unitPrice).setScale(0, RoundingMode.HALF_UP);
        if (amount.signum() == 0 && basis != CalculationBasis.PER_RESIDENT
                && basis != CalculationBasis.PER_MOTORCYCLE && basis != CalculationBasis.PER_CAR) {
            throw new IllegalArgumentException("Mức phí phải tạo ra số tiền lớn hơn 0 VND.");
        }
        return amount;
    }

    public static ChargeStatus status(BigDecimal amount, BigDecimal paid) {
        if (amount == null || amount.signum() < 0 || paid == null || paid.signum() < 0 || paid.compareTo(amount) > 0) {
            throw new IllegalArgumentException("Dữ liệu khoản phải thu không hợp lệ.");
        }
        if (amount.signum() == 0) return ChargeStatus.PAID;
        if (paid.signum() == 0) return ChargeStatus.UNPAID;
        return paid.compareTo(amount) == 0 ? ChargeStatus.PAID : ChargeStatus.PARTIALLY_PAID;
    }

    public static void validatePayment(BigDecimal amount, BigDecimal balance) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Số tiền thu phải lớn hơn 0.");
        }
        if (amount.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Số tiền thu phải là số nguyên VND.");
        }
        if (balance == null || balance.signum() <= 0) {
            throw new IllegalArgumentException("Khoản phải thu không còn số dư cần thanh toán.");
        }
    }
}
