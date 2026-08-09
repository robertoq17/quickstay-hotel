package com.quickstay.saga.client;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentClientResponse(UUID paymentId, UUID reservationId, BigDecimal amount, String status) {
    public boolean authorized() { return "AUTHORIZED".equals(status); }
}
