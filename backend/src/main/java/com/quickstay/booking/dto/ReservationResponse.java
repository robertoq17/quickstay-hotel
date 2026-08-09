package com.quickstay.booking.dto;

import com.quickstay.booking.domain.ReservationStatus;

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
