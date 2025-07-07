package com.fastshop.domain.model;

public enum OrderStatus {
    PAID("PAGADO"),
    FAILED("FAILED");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}