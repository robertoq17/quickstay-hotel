package com.quickstay.saga.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "saga_executions")
@Getter
@Setter
@NoArgsConstructor
public class SagaExecution {
    @Id
    private UUID id;

    @Column(unique = true)
    private UUID reservationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;

    @Column(nullable = false, length = 80)
    private String currentStep;

    @Column(length = 500)
    private String errorMessage;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
