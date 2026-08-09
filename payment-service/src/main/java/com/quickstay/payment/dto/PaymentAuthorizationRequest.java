package com.quickstay.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentAuthorizationRequest(
        @NotNull UUID reservationId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        boolean failPayment) {}
