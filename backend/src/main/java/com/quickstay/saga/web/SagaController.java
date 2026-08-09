package com.quickstay.saga.web;

import com.quickstay.saga.dto.SagaBookingRequest;
import com.quickstay.saga.dto.SagaBookingResponse;
import com.quickstay.saga.service.SagaOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sagas/bookings")
@RequiredArgsConstructor
public class SagaController {
    private final SagaOrchestrator sagaOrchestrator;

    @PostMapping
    public ResponseEntity<SagaBookingResponse> bookAndPay(@Valid @RequestBody SagaBookingRequest request) {
        SagaBookingResponse response = sagaOrchestrator.execute(request);
        return ResponseEntity.status(response.sagaStatus().name().equals("COMPLETED")
                ? HttpStatus.CREATED : HttpStatus.CONFLICT).body(response);
    }

    @GetMapping("/{sagaId}")
    public ResponseEntity<?> get(@PathVariable UUID sagaId) {
        return ResponseEntity.ok(sagaOrchestrator.get(sagaId));
    }
}
