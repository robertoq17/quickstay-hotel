package com.quickstay.controller;

import com.quickstay.dto.ReservationRequest;
import com.quickstay.dto.ReservationResponse;
import com.quickstay.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.reserve(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.cancel(id));
    }
}
