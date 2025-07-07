package com.fastshop.application.dto;

import java.math.BigDecimal;

public class OrderItemResponse {
    private Integer productId;
    private Integer cantidad;
    private BigDecimal unitPrice;

    public OrderItemResponse() {}

    public OrderItemResponse(Integer productId, Integer cantidad, BigDecimal unitPrice) {
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
}