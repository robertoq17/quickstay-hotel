package com.quickstay.payment.dto;

import com.quickstay.payment.domain.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(UUID paymentId, UUID reservationId, BigDecimal amount, PaymentStatus status) {}
