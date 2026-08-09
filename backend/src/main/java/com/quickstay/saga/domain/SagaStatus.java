package com.quickstay.saga.domain;

public enum SagaStatus {
    STARTED,
    RESERVATION_CREATED,
    PAYMENT_AUTHORIZED,
    COMPLETED,
    COMPENSATED,
    FAILED
}
