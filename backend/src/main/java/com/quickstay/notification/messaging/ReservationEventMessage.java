package com.quickstay.notification.messaging;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Copia, del lado de Notification, del contrato de mensaje. Tiene los
 * mismos campos que {@code booking.messaging.ReservationIntegrationEvent}
 * a propósito — pero es una clase distinta. Notification nunca importa
 * nada del paquete {@code booking}: solo conoce la forma del JSON que
 * llega por la cola. Esto es lo que permite que, en una sesión futura,
 * Notification se convierta en su propio servicio sin romper compilación
 * cruzada con Booking.
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
