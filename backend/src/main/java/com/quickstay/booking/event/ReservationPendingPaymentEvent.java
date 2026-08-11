package com.quickstay.booking.event;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain event published when a Saga creates a temporary reservation.
 * Inventory uses it to represent the room as unavailable in its CQRS
 * read model while payment is still in progress.
 */
public record ReservationPendingPaymentEvent(
        UUID reservationId,
        UUID roomId,
        String guestFullName,
        String guestEmail,
        LocalDate checkIn,
        LocalDate checkOut
) {
}
