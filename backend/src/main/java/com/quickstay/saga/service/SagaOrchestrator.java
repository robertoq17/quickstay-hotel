package com.quickstay.saga.service;

import com.quickstay.booking.dto.ReservationRequest;
import com.quickstay.booking.dto.ReservationResponse;
import com.quickstay.booking.service.ReservationService;
import com.quickstay.payment.dto.PaymentResponse;
import com.quickstay.payment.service.PaymentService;
import com.quickstay.saga.domain.SagaExecution;
import com.quickstay.saga.domain.SagaStatus;
import com.quickstay.saga.dto.SagaBookingRequest;
import com.quickstay.saga.dto.SagaBookingResponse;
import com.quickstay.saga.repository.SagaExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Orchestration-based Saga for Booking -> Payment -> Booking confirmation.
 *
 * The orchestrator owns the workflow state. When payment fails, it invokes
 * Booking.cancel() as a compensating action. If a payment was already
 * authorized and a later step fails, it invokes Payment.refund() before
 * cancelling the reservation. The operation is intentionally idempotent for
 * already completed reservations/payments.
 */
@Service
@RequiredArgsConstructor
public class SagaOrchestrator {
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final SagaExecutionRepository sagaRepository;

    public SagaBookingResponse execute(SagaBookingRequest request) {
        ReservationRequest reservationRequest = request.reservation();
        ReservationResponse reservation = null;
        PaymentResponse payment = null;
        SagaExecution saga = newSaga();
        sagaRepository.save(saga);

        try {
            reservation = reservationService.reservePendingPayment(reservationRequest);
            saga.setReservationId(reservation.reservationId());
            saga.setStatus(SagaStatus.RESERVATION_CREATED);
            saga.setCurrentStep("PAYMENT_AUTHORIZATION");
            touch(saga);
            sagaRepository.save(saga);

            payment = paymentService.authorize(reservation.reservationId(), request.paymentAmount(), request.failPayment());
            saga.setStatus(SagaStatus.PAYMENT_AUTHORIZED);
            saga.setCurrentStep("BOOKING_CONFIRMATION");
            touch(saga);
            sagaRepository.save(saga);

            reservation = reservationService.confirm(reservation.reservationId());
            saga.setStatus(SagaStatus.COMPLETED);
            saga.setCurrentStep("COMPLETED");
            touch(saga);
            sagaRepository.save(saga);

            return new SagaBookingResponse(saga.getId(), saga.getStatus(), saga.getCurrentStep(), reservation, payment,
                    "Booking Saga completed successfully.");
        } catch (Exception failure) {
            if (reservation != null) {
                compensate(reservation.reservationId(), payment);
            }

            saga.setStatus(SagaStatus.COMPENSATED);
            saga.setCurrentStep("COMPENSATION_COMPLETED");
            saga.setErrorMessage(shortMessage(failure));
            touch(saga);
            sagaRepository.save(saga);

            ReservationResponse compensatedReservation = reservation != null
                    ? reservationService.cancel(reservation.reservationId())
                    : null;

            return new SagaBookingResponse(saga.getId(), saga.getStatus(), saga.getCurrentStep(),
                    compensatedReservation, payment,
                    "Booking Saga failed and compensating actions were executed: " + shortMessage(failure));
        }
    }

    public SagaExecution get(UUID sagaId) {
        return sagaRepository.findById(sagaId)
                .orElseThrow(() -> new NoSuchElementException("Saga not found: " + sagaId));
    }

    private void compensate(UUID reservationId, PaymentResponse payment) {
        if (payment != null && payment.status() == com.quickstay.payment.domain.PaymentStatus.AUTHORIZED) {
            paymentService.refund(reservationId);
        }
    }

    private SagaExecution newSaga() {
        SagaExecution saga = new SagaExecution();
        saga.setStatus(SagaStatus.STARTED);
        saga.setCurrentStep("RESERVATION_CREATION");
        saga.setId(UUID.randomUUID());
        saga.setCreatedAt(Instant.now());
        saga.setUpdatedAt(Instant.now());
        return saga;
    }

    private void touch(SagaExecution saga) {
        saga.setUpdatedAt(Instant.now());
    }

    private String shortMessage(Exception failure) {
        String message = failure.getMessage();
        return message == null ? failure.getClass().getSimpleName() : message.substring(0, Math.min(message.length(), 500));
    }
}
