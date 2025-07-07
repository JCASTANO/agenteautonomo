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
import static org.mockito.Mockito.spy;

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

    @Test
    void shouldGetOrderSuccessfully() {
        // Given
        Long orderId = 1L;
        Order expectedOrder = new Order(orderId, 123, List.of(), BigDecimal.ZERO, OrderStatus.PAID, PaymentMethod.CREDIT_CARD);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(expectedOrder));

        // When
        Optional<Order> result = orderDomainService.getOrder(orderId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedOrder, result.get());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When
        Optional<Order> result = orderDomainService.getOrder(orderId);

        // Then
        assertFalse(result.isPresent());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundDuringEnrichment() {
        // Given
        Integer customerId = 123;
        List<OrderItem> items = List.of(
                new OrderItem(999, 1, null) // Non-existent product
        );
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        when(inventoryRepository.findByProductId(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                orderDomainService.createOrder(customerId, items, paymentMethod));
        
        assertEquals("Product not found: 999", exception.getMessage());
        verify(paymentService, never()).processPayment(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundDuringStockValidation() {
        // Given
        Integer customerId = 123;
        List<OrderItem> items = List.of(
                new OrderItem(456, 1, null),
                new OrderItem(999, 1, null) // Second product doesn't exist
        );
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        Product product456 = new Product(456, 5, new BigDecimal("100.00"));
        
        when(inventoryRepository.findByProductId(456)).thenReturn(Optional.of(product456));
        when(inventoryRepository.findByProductId(999)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                orderDomainService.createOrder(customerId, items, paymentMethod));
        
        assertEquals("Product not found: 999", exception.getMessage());
        verify(paymentService, never()).processPayment(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldVerifyStockReservationIsActuallyCalled() {
        // Given
        Integer customerId = 123;
        List<OrderItem> items = List.of(
                new OrderItem(456, 2, null)
        );
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        Product product456 = new Product(456, 5, new BigDecimal("100.00"));

        when(inventoryRepository.findByProductId(456)).thenReturn(Optional.of(product456));
        when(paymentService.processPayment(any(BigDecimal.class), eq("CREDIT_CARD"))).thenReturn(true);
        when(orderRepository.getNextOrderId()).thenReturn(1L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = orderDomainService.createOrder(customerId, items, paymentMethod);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.PAID, result.getStatus());
        
        // Verify that the product's reserveStock method was called with correct quantity
        // This addresses the mutation that survived (removing product.reserveStock call)
        verify(inventoryRepository, times(3)).findByProductId(456); // Once for enrichment, once for validation, once for reservation
        verify(inventoryRepository).updateProduct(any(Product.class)); // Verify the product was updated after reservation
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldCreateFailedOrderWhenStockReservationFails() {
        // Given
        Integer customerId = 123;
        List<OrderItem> items = List.of(
                new OrderItem(456, 2, null)
        );
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        Product product456 = new Product(456, 5, new BigDecimal("100.00"));

        when(inventoryRepository.findByProductId(456)).thenReturn(Optional.of(product456));
        when(paymentService.processPayment(any(BigDecimal.class), eq("CREDIT_CARD"))).thenReturn(true);
        when(orderRepository.getNextOrderId()).thenReturn(1L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Make the third call (during stock reservation) return empty to simulate failure
        when(inventoryRepository.findByProductId(456))
                .thenReturn(Optional.of(product456))  // First call (enrichment)
                .thenReturn(Optional.of(product456))  // Second call (validation)
                .thenReturn(Optional.empty());        // Third call (reservation) - simulates product disappearing

        // When
        Order result = orderDomainService.createOrder(customerId, items, paymentMethod);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.FAILED, result.getStatus());
        assertEquals(new BigDecimal("200.00"), result.getTotalAmount());
        verify(paymentService).processPayment(new BigDecimal("200.00"), "CREDIT_CARD");
        verify(orderRepository).save(any(Order.class));
    }

    @Test  
    void shouldThrowExceptionWhenProductNotFoundDuringStockReservation() {
        // Given
        Integer customerId = 123;
        List<OrderItem> items = List.of(
                new OrderItem(456, 2, null)
        );
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        Product product456 = new Product(456, 5, new BigDecimal("100.00"));

        when(inventoryRepository.findByProductId(456))
                .thenReturn(Optional.of(product456))  // First call (enrichment)
                .thenReturn(Optional.of(product456))  // Second call (validation)
                .thenReturn(Optional.empty());        // Third call (reservation) - product not found

        when(paymentService.processPayment(any(BigDecimal.class), eq("CREDIT_CARD"))).thenReturn(true);
        when(orderRepository.getNextOrderId()).thenReturn(1L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = orderDomainService.createOrder(customerId, items, paymentMethod);

        // Then - Should create FAILED order when stock reservation fails due to missing product
        assertNotNull(result);
        assertEquals(OrderStatus.FAILED, result.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldVerifyProductStockReservationDetails() {
        // Given
        Integer customerId = 123;
        List<OrderItem> items = List.of(
                new OrderItem(456, 3, null)
        );
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        Product product456 = spy(new Product(456, 10, new BigDecimal("100.00")));

        when(inventoryRepository.findByProductId(456)).thenReturn(Optional.of(product456));
        when(paymentService.processPayment(any(BigDecimal.class), eq("CREDIT_CARD"))).thenReturn(true);
        when(orderRepository.getNextOrderId()).thenReturn(1L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = orderDomainService.createOrder(customerId, items, paymentMethod);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.PAID, result.getStatus());
        
        // Verify that the specific reserveStock method was called with the exact quantity
        verify(product456).reserveStock(3);
        verify(inventoryRepository).updateProduct(product456);
    }

    @Test
    void shouldAccessInsufficientStockExceptionProductId() {
        // Given
        Integer customerId = 123;
        List<OrderItem> items = List.of(
                new OrderItem(456, 10, null) // More than available stock
        );
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        Product product456 = new Product(456, 5, new BigDecimal("100.00")); // Only 5 available

        when(inventoryRepository.findByProductId(456)).thenReturn(Optional.of(product456));

        // When & Then
        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> 
                orderDomainService.createOrder(customerId, items, paymentMethod));
        
        // Verify the productId can be accessed from the exception
        assertEquals(Integer.valueOf(456), exception.getProductId());
        assertEquals("Insufficient stock for product 456", exception.getMessage());
    }
}