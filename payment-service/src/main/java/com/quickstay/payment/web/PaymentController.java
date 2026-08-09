package com.quickstay.payment.web;

import com.quickstay.payment.domain.PaymentStatus;
import com.quickstay.payment.dto.PaymentAuthorizationRequest;
import com.quickstay.payment.dto.PaymentResponse;
import com.quickstay.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/authorize")
    public ResponseEntity<PaymentResponse> authorize(@Valid @RequestBody PaymentAuthorizationRequest request) {
        PaymentResponse response = paymentService.authorize(request.reservationId(), request.amount(), request.failPayment());
        return ResponseEntity.status(response.status() == PaymentStatus.FAILED ? HttpStatus.CONFLICT : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/{reservationId}/refund")
    public ResponseEntity<PaymentResponse> refund(@PathVariable UUID reservationId) {
        return ResponseEntity.ok(paymentService.refund(reservationId));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("quickstay-payment-service: UP");
    }
}
