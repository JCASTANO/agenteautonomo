package com.fastshop.domain.port;

import java.math.BigDecimal;

public interface PaymentService {
    boolean processPayment(BigDecimal amount, String paymentMethod);
}