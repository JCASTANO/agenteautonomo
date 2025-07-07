package com.fastshop.application.usecase;

import com.fastshop.application.dto.*;
import com.fastshop.domain.model.*;
import com.fastshop.domain.service.OrderDomainService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreateOrderUseCase {
    private final OrderDomainService orderDomainService;

    public CreateOrderUseCase(OrderDomainService orderDomainService) {
        this.orderDomainService = orderDomainService;
    }

    public OrderResponse execute(CreateOrderRequest request) {
        List<OrderItem> items = request.getItems().stream()
                .map(item -> new OrderItem(item.getProductId(), item.getCantidad(), null))
                .collect(Collectors.toList());

        PaymentMethod paymentMethod = PaymentMethod.fromString(request.getPaymentMethod());
        
        Order order = orderDomainService.createOrder(request.getCustomerId(), items, paymentMethod);
        
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getProductId(), item.getCantidad(), item.getUnitPrice()))
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getOrderId(),
                order.getCustomerId(),
                itemResponses,
                order.getTotalAmount(),
                order.getStatus().getValue()
        );
    }
}