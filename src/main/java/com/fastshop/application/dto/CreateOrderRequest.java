package com.fastshop.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public class CreateOrderRequest {
    @NotNull(message = "Customer ID is required")
    private Integer customerId;

    @NotEmpty(message = "Items cannot be empty")
    private List<OrderItemRequest> items;

    @NotNull(message = "Payment method is required")
    @Pattern(regexp = "CREDIT_CARD", message = "Medio de pago no permitido")
    private String paymentMethod;

    public CreateOrderRequest() {}

    public CreateOrderRequest(Integer customerId, List<OrderItemRequest> items, String paymentMethod) {
        this.customerId = customerId;
        this.items = items;
        this.paymentMethod = paymentMethod;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}