package com.fastshop.domain.model;

import java.math.BigDecimal;

public class Product {
    private Integer productId;
    private Integer stock;
    private BigDecimal unitPrice;

    public Product() {}

    public Product(Integer productId, Integer stock, BigDecimal unitPrice) {
        this.productId = productId;
        this.stock = stock;
        this.unitPrice = unitPrice;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public boolean hasAvailableStock(Integer requestedQuantity) {
        return stock != null && requestedQuantity != null && stock >= requestedQuantity;
    }

    public void reserveStock(Integer quantity) {
        if (!hasAvailableStock(quantity)) {
            throw new IllegalStateException("Insufficient stock for product " + productId);
        }
        this.stock -= quantity;
    }
}