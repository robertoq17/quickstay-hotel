package com.quickstay.saga.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentServiceClient {
    private final RestClient.Builder restClientBuilder;

    @Value("${quickstay.payment.base-url:http://127.0.0.1:8081}")
    private String baseUrl;

    public PaymentClientResponse authorize(UUID reservationId, BigDecimal amount, boolean failPayment) {
        try {
            PaymentClientResponse response = restClientBuilder.baseUrl(baseUrl).build()
                    .post()
                    .uri("/api/payments/authorize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AuthorizationRequest(reservationId, amount, failPayment))
                    .retrieve()
                    .body(PaymentClientResponse.class);

            if (response == null) {
                throw new PaymentServiceUnavailableException("Payment Service returned an empty response");
            }
            if (!response.authorized()) {
                throw new PaymentAuthorizationException("Payment Service rejected authorization for reservation " + reservationId);
            }
            return response;
        } catch (RestClientResponseException ex) {
            throw new PaymentAuthorizationException("Payment Service rejected authorization for reservation " + reservationId);
        } catch (PaymentAuthorizationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PaymentServiceUnavailableException("Payment Service is unavailable: " + ex.getMessage(), ex);
        }
    }

    public PaymentClientResponse refund(UUID reservationId) {
        try {
            PaymentClientResponse response = restClientBuilder.baseUrl(baseUrl).build()
                    .post()
                    .uri("/api/payments/{reservationId}/refund", reservationId)
                    .retrieve()
                    .body(PaymentClientResponse.class);
            if (response == null) {
                throw new PaymentServiceUnavailableException("Payment Service returned an empty refund response");
            }
            return response;
        } catch (Exception ex) {
            if (ex instanceof PaymentServiceUnavailableException) throw (PaymentServiceUnavailableException) ex;
            throw new PaymentServiceUnavailableException("Payment refund failed: " + ex.getMessage(), ex);
        }
    }

    private record AuthorizationRequest(UUID reservationId, BigDecimal amount, boolean failPayment) {}

    public static class PaymentAuthorizationException extends RuntimeException {
        public PaymentAuthorizationException(String message) { super(message); }
    }

    public static class PaymentServiceUnavailableException extends RuntimeException {
        public PaymentServiceUnavailableException(String message) { super(message); }
        public PaymentServiceUnavailableException(String message, Throwable cause) { super(message, cause); }
    }
}
