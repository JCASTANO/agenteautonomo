package com.fastshop.domain.model;

import java.math.BigDecimal;

public class OrderItem {
    private Integer productId;
    private Integer cantidad;
    private BigDecimal unitPrice;

    public OrderItem() {}

    public OrderItem(Integer productId, Integer cantidad, BigDecimal unitPrice) {
        this.productId = productId;
        this.cantidad = cantidad;
        this.unitPrice = unitPrice;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        if (unitPrice == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(cantidad));
    }
}