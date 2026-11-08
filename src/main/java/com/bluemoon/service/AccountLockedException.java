package com.bluemoon.service;

public final class AccountLockedException extends RuntimeException {
    public AccountLockedException() {
        super("Tài khoản đang tạm khóa. Vui lòng thử lại sau.");
    }
}
