package com.quickstay.payment.service;

import com.quickstay.payment.domain.Payment;
import com.quickstay.payment.domain.PaymentStatus;
import com.quickstay.payment.dto.PaymentResponse;
import com.quickstay.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse authorize(UUID reservationId, BigDecimal amount, boolean failPayment) {
        return paymentRepository.findByReservationId(reservationId)
                .map(existing -> {
                    if (existing.getStatus() == PaymentStatus.AUTHORIZED) {
                        return toResponse(existing);
                    }
                    if (existing.getStatus() == PaymentStatus.FAILED) {
                        return toResponse(existing);
                    }
                    throw new PaymentAuthorizationException(
                            "Payment for reservation " + reservationId + " is already in status " + existing.getStatus());
                })
                .orElseGet(() -> authorizeNew(reservationId, amount, failPayment));
    }

    @Transactional
    public PaymentResponse refund(UUID reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found for reservation: " + reservationId));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return toResponse(payment);
        }
        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new IllegalStateException("Payment cannot be refunded from status " + payment.getStatus());
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        return toResponse(paymentRepository.save(payment));
    }

    private PaymentResponse authorizeNew(UUID reservationId, BigDecimal amount, boolean failPayment) {
        Payment payment = new Payment();
        payment.setReservationId(reservationId);
        payment.setAmount(amount);
        payment.setCreatedAt(Instant.now());
        payment.setStatus(failPayment ? PaymentStatus.FAILED : PaymentStatus.AUTHORIZED);
        return toResponse(paymentRepository.save(payment));
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getReservationId(), payment.getAmount(), payment.getStatus());
    }

    public static class PaymentAuthorizationException extends RuntimeException {
        public PaymentAuthorizationException(String message) {
            super(message);
        }
    }
}
