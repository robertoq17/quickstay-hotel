package com.quickstay.booking.event;

import java.util.UUID;

/**
 * Domain event interno del Bounded Context Booking (ver
 * {@link ReservationConfirmedEvent} para la nota de diseño general).
 */
public record ReservationCancelledEvent(
        UUID reservationId,
        UUID roomId,
        String guestEmail
) {
}
