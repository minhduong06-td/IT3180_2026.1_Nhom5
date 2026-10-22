package com.bluemoon.service;

public final class HouseholdHeadExistsException extends RuntimeException {
    private final String headName;

    public HouseholdHeadExistsException(String headName) {
        super("Hộ đã có chủ hộ là " + headName + ".");
        this.headName = headName;
    }

    public String headName() {
        return headName;
    }
}
