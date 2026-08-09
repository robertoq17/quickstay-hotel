package com.quickstay.dto;

import com.quickstay.domain.ReservationStatus;

import java.time.LocalDate;
import java.util.UUID;

public record ReservationResponse(
        UUID reservationId,
        UUID roomId,
        String guestEmail,
        LocalDate checkIn,
        LocalDate checkOut,
        ReservationStatus status
) {
}
