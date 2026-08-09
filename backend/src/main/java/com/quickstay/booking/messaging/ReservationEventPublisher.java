package com.quickstay.booking.messaging;

import com.quickstay.booking.event.ReservationCancelledEvent;
import com.quickstay.booking.event.ReservationConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

/**
 * Adaptador de infraestructura: escucha los domain events publicados en
 * memoria por {@code ReservationService} (Actividad 2 — in-memory event
 * bus) y los reenvía como mensajes RabbitMQ (Actividad 3 — broker externo).
 *
 * {@code TransactionPhase.AFTER_COMMIT} es la parte clave: si la
 * transacción de reserve()/cancel() falla o se revierte, el mensaje NUNCA
 * sale a RabbitMQ. Esto evita notificar al huésped de una reserva que en
 * realidad no se guardó.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationConfirmed(ReservationConfirmedEvent event) {
        ReservationIntegrationEvent message = new ReservationIntegrationEvent(
                ReservationIntegrationEvent.TYPE_CONFIRMED,
                event.reservationId(),
                event.roomId(),
                event.guestFullName(),
                event.guestEmail(),
                event.checkIn(),
                event.checkOut(),
                Instant.now()
        );

        log.info("Publishing {} to exchange={} routingKey={}",
                message.eventType(), BookingMessagingConfig.RESERVATION_EXCHANGE,
                BookingMessagingConfig.ROUTING_KEY_CONFIRMED);

        rabbitTemplate.convertAndSend(
                BookingMessagingConfig.RESERVATION_EXCHANGE,
                BookingMessagingConfig.ROUTING_KEY_CONFIRMED,
                message);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationCancelled(ReservationCancelledEvent event) {
        ReservationIntegrationEvent message = new ReservationIntegrationEvent(
                ReservationIntegrationEvent.TYPE_CANCELLED,
                event.reservationId(),
                event.roomId(),
                null,
                event.guestEmail(),
                null,
                null,
                Instant.now()
        );

        log.info("Publishing {} to exchange={} routingKey={}",
                message.eventType(), BookingMessagingConfig.RESERVATION_EXCHANGE,
                BookingMessagingConfig.ROUTING_KEY_CANCELLED);

        rabbitTemplate.convertAndSend(
                BookingMessagingConfig.RESERVATION_EXCHANGE,
                BookingMessagingConfig.ROUTING_KEY_CANCELLED,
                message);
    }
}
