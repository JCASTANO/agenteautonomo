package com.fastshop.domain.service;

import com.fastshop.domain.model.*;
import com.fastshop.domain.port.InventoryRepository;
import com.fastshop.domain.port.OrderRepository;
import com.fastshop.domain.port.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderDomainService {
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final PaymentService paymentService;

    public OrderDomainService(OrderRepository orderRepository,
                             InventoryRepository inventoryRepository,
                             PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.paymentService = paymentService;
    }

    public Order createOrder(Integer customerId, List<OrderItem> items, PaymentMethod paymentMethod) {
        // Validate and enrich items with prices
        enrichItemsWithPrices(items);
        
        // Check stock availability
        validateStockAvailability(items);
        
        // Calculate total amount
        BigDecimal totalAmount = calculateTotalAmount(items);
        
        // Process payment
        boolean paymentSuccessful = paymentService.processPayment(totalAmount, paymentMethod.getValue());
        
        Long orderId = orderRepository.getNextOrderId();
        Order order;
        
        if (!paymentSuccessful) {
            // Save order as FAILED
            order = new Order(orderId, customerId, items, totalAmount, OrderStatus.FAILED, paymentMethod);
            return orderRepository.save(order);
        }
        
        // Reserve stock
        try {
            reserveStock(items);
        } catch (Exception e) {
            // Save order as FAILED if stock reservation fails
            order = new Order(orderId, customerId, items, totalAmount, OrderStatus.FAILED, paymentMethod);
            return orderRepository.save(order);
        }
        
        // Save order as PAID
        order = new Order(orderId, customerId, items, totalAmount, OrderStatus.PAID, paymentMethod);
        return orderRepository.save(order);
    }

    public Optional<Order> getOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }

    private void enrichItemsWithPrices(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
            item.setUnitPrice(product.getUnitPrice());
        }
    }

    private void validateStockAvailability(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
            
            if (!product.hasAvailableStock(item.getCantidad())) {
                throw new InsufficientStockException("Insufficient stock for product " + item.getProductId(), item.getProductId());
            }
        }
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void reserveStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + item.getProductId()));
            
            product.reserveStock(item.getCantidad());
            inventoryRepository.updateProduct(product);
        }
    }
}