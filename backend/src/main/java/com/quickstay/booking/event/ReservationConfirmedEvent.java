package com.quickstay.booking.event;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain event interno del Bounded Context Booking. Se publica en memoria
 * vía {@link org.springframework.context.ApplicationEventPublisher} — no
 * sabe nada de RabbitMQ ni de ningún broker externo. La traducción a un
 * mensaje que sale del proceso ocurre en la capa de infraestructura
 * ({@code booking.messaging.ReservationEventPublisher}).
 */
public record ReservationConfirmedEvent(
        UUID reservationId,
        UUID roomId,
        String guestFullName,
        String guestEmail,
        LocalDate checkIn,
        LocalDate checkOut
) {
}
