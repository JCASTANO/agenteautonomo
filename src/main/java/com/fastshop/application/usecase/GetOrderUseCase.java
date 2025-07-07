package com.fastshop.application.usecase;

import com.fastshop.application.dto.OrderItemResponse;
import com.fastshop.application.dto.OrderResponse;
import com.fastshop.domain.model.Order;
import com.fastshop.domain.service.OrderDomainService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GetOrderUseCase {
    private final OrderDomainService orderDomainService;

    public GetOrderUseCase(OrderDomainService orderDomainService) {
        this.orderDomainService = orderDomainService;
    }

    public Optional<OrderResponse> execute(Long orderId) {
        Optional<Order> order = orderDomainService.getOrder(orderId);
        return order.map(this::mapToResponse);
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