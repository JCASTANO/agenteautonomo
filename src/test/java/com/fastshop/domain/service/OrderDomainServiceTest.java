package com.fastshop.domain.service;

import com.fastshop.domain.model.*;
import com.fastshop.domain.port.InventoryRepository;
import com.fastshop.domain.port.OrderRepository;
import com.fastshop.domain.port.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderDomainServiceTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private InventoryRepository inventoryRepository;
    
    @Mock
    private PaymentService paymentService;

    private OrderDomainService orderDomainService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderDomainService = new OrderDomainService(orderRepository, inventoryRepository, paymentService);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        Integer customerId = 123;
        List<OrderItem> items = List.of(
                new OrderItem(456, 2, null),
                new OrderItem(789, 1, null)
        );
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        Product product456 = new Product(456, 5, new BigDecimal("100.00"));
        Product product789 = new Product(789, 2, new BigDecimal("250.00"));

        when(inventoryRepository.findByProductId(456)).thenReturn(Optional.of(product456));
        when(inventoryRepository.findByProductId(789)).thenReturn(Optional.of(product789));
        when(paymentService.processPayment(any(BigDecimal.class), eq("CREDIT_CARD"))).thenReturn(true);
        when(orderRepository.getNextOrderId()).thenReturn(1L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = orderDomainService.createOrder(customerId, items, paymentMethod);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getOrderId());
        assertEquals(customerId, result.getCustomerId());
        assertEquals(OrderStatus.PAID, result.getStatus());
        assertEquals(new BigDecimal("450.00"), result.getTotalAmount());
        
        verify(inventoryRepository, atLeastOnce()).findByProductId(any());
        verify(paymentService).processPayment(new BigDecimal("450.00"), "CREDIT_CARD");
        verify(inventoryRepository, times(2)).updateProduct(any(Product.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldFailOrderWhenInsufficientStock() {
        // Given
        Integer customerId = 123;
        List<OrderItem> items = List.of(
                new OrderItem(456, 10, null) // More than available stock
        );
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        Product product456 = new Product(456, 5, new BigDecimal("100.00")); // Only 5 available

        when(inventoryRepository.findByProductId(456)).thenReturn(Optional.of(product456));

        // When & Then
        assertThrows(InsufficientStockException.class, () -> 
                orderDomainService.createOrder(customerId, items, paymentMethod));
        
        verify(paymentService, never()).processPayment(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldCreateFailedOrderWhenPaymentFails() {
        // Given
        Integer customerId = 123;
        List<OrderItem> items = List.of(
                new OrderItem(456, 2, null)
        );
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        Product product456 = new Product(456, 5, new BigDecimal("100.00"));

        when(inventoryRepository.findByProductId(456)).thenReturn(Optional.of(product456));
        when(paymentService.processPayment(any(BigDecimal.class), eq("CREDIT_CARD"))).thenReturn(false);
        when(orderRepository.getNextOrderId()).thenReturn(1L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = orderDomainService.createOrder(customerId, items, paymentMethod);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.FAILED, result.getStatus());
        verify(inventoryRepository, never()).updateProduct(any(Product.class)); // Stock should not be reserved
        verify(orderRepository).save(any(Order.class));
    }
}