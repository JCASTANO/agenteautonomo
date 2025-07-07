package com.fastshop.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderItemRequest {
    @NotNull(message = "Product ID is required")
    private Integer productId;

    @NotNull(message = "Cantidad is required")
    @Min(value = 1, message = "Cantidad must be at least 1")
    @Max(value = 10, message = "Cantidad cannot exceed 10 units")
    private Integer cantidad;

    public OrderItemRequest() {}

    public OrderItemRequest(Integer productId, Integer cantidad) {
        this.productId = productId;
        this.cantidad = cantidad;
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
}