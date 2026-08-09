package com.quickstay.saga.dto;

import com.quickstay.booking.dto.ReservationResponse;
import com.quickstay.saga.client.PaymentClientResponse;
import com.quickstay.saga.domain.SagaStatus;

import java.util.UUID;

public record SagaBookingResponse(
        UUID sagaId,
        SagaStatus sagaStatus,
        String currentStep,
        ReservationResponse reservation,
        PaymentClientResponse payment,
        String message
) {}
