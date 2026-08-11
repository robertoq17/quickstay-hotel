package com.quickstay.inventory.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * CQRS projection consumer.
 *
 * This component is intentionally a consumer of the external message
 * contract, not of Booking domain events. It keeps Inventory's read model
 * synchronized asynchronously with Booking's write model.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryAvailabilityProjectionListener {

    private final JdbcTemplate jdbcTemplate;

    @RabbitListener(queues = InventoryProjectionMessagingConfig.QUEUE_NAME)
    @Transactional
    public void onReservationEvent(ReservationEventMessage message) {
        switch (message.eventType()) {
            case "RESERVATION_PENDING_PAYMENT" -> upsertReservation(message, "PENDING_PAYMENT");
            case "RESERVATION_CONFIRMED" -> upsertReservation(message, "CONFIRMED");
            case "RESERVATION_CANCELLED" -> markCancelled(message);
            default -> log.warn("[CQRS] Tipo de evento no reconocido: {}", message.eventType());
        }
    }

    private void upsertReservation(ReservationEventMessage message, String status) {
        jdbcTemplate.update("""
                INSERT INTO quickstay_read.reservation_availability_read_model
                    (reservation_id, room_id, check_in, check_out, status, updated_at)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON CONFLICT (reservation_id) DO UPDATE SET
                    room_id = EXCLUDED.room_id,
                    check_in = EXCLUDED.check_in,
                    check_out = EXCLUDED.check_out,
                    status = EXCLUDED.status,
                    updated_at = CURRENT_TIMESTAMP
                """,
                message.reservationId(),
                message.roomId(),
                message.checkIn(),
                message.checkOut(),
                status
        );

        log.info("[CQRS] Read model actualizado: reservation={} status={} room={}",
                message.reservationId(), status, message.roomId());
    }

    private void markCancelled(ReservationEventMessage message) {
        jdbcTemplate.update("""
                UPDATE quickstay_read.reservation_availability_read_model
                SET status = 'CANCELLED', updated_at = CURRENT_TIMESTAMP
                WHERE reservation_id = ?
                """, message.reservationId());

        log.info("[CQRS] Read model liberado: reservation={} room={}",
                message.reservationId(), message.roomId());
    }
}
