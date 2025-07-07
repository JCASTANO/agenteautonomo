package com.fastshop.application.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderResponse {
    private Long orderId;
    private Integer customerId;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private String status;

    public OrderResponse() {}

    public OrderResponse(Long orderId, Integer customerId, List<OrderItemResponse> items, 
                        BigDecimal totalAmount, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}