package com.quickstay.saga.dto;

import com.quickstay.booking.dto.ReservationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SagaBookingRequest(
        @Valid @NotNull ReservationRequest reservation,
        @NotNull @DecimalMin("0.01") BigDecimal paymentAmount,
        boolean failPayment
) {}
