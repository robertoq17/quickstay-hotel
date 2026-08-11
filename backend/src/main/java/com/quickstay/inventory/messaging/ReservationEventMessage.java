package com.quickstay.inventory.messaging;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Inventory's own copy of the RabbitMQ integration contract.
 *
 * It intentionally does not import Booking or Notification classes. Each
 * consumer owns the representation it needs from the external message.
 */
public record ReservationEventMessage(
        String eventType,
        UUID reservationId,
        UUID roomId,
        String guestFullName,
        String guestEmail,
        LocalDate checkIn,
        LocalDate checkOut,
        Instant occurredAt
) {
}
