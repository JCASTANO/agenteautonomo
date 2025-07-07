package com.fastshop.domain.service;

public class InsufficientStockException extends RuntimeException {
    private final Integer productId;

    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
    }

    public InsufficientStockException(String message, Integer productId) {
        super(message);
        this.productId = productId;
    }

    public Integer getProductId() {
        return productId;
    }
}