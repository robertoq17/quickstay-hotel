package com.quickstay.saga.repository;

import com.quickstay.saga.domain.SagaExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SagaExecutionRepository extends JpaRepository<SagaExecution, UUID> {
    Optional<SagaExecution> findByReservationId(UUID reservationId);
}
