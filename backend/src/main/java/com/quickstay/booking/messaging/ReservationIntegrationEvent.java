package com.quickstay.booking.messaging;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Integration Event: el contrato ESTABLE que efectivamente viaja por
 * RabbitMQ como JSON. Deliberadamente separado del domain event interno
 * ({@code booking.event.*}) — ver docs/session-04-domain-events.md.
 *
 * Los consumidores (ej. Notification) tienen su propia copia de esta forma
 * de mensaje; no dependen de esta clase Java.
 */
public record ReservationIntegrationEvent(
        String eventType,
        UUID reservationId,
        UUID roomId,
        String guestFullName,
        String guestEmail,
        LocalDate checkIn,
        LocalDate checkOut,
        Instant occurredAt
) {
    public static final String TYPE_CONFIRMED = "RESERVATION_CONFIRMED";
    public static final String TYPE_CANCELLED = "RESERVATION_CANCELLED";
}
