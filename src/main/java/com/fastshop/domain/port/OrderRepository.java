package com.fastshop.domain.port;

import com.fastshop.domain.model.Order;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long orderId);
    Long getNextOrderId();
}