package com.fastshop.infrastructure.adapter.service;

import com.fastshop.domain.port.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

@Service
public class SimulatedPaymentService implements PaymentService {
    private final Random random = new Random();

    @Override
    public boolean processPayment(BigDecimal amount, String paymentMethod) {
        if (!"CREDIT_CARD".equals(paymentMethod)) {
            return false;
        }
        
        // Simulate payment processing with configurable success rate
        // For demo purposes, we can simulate failures by checking amount
        // In a real system, this would integrate with actual payment processors
        return amount.compareTo(new BigDecimal("1000")) < 0; // Fail for orders > $1000
    }
}