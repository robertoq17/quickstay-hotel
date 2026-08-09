package com.quickstay.notification.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor del dominio Notification. Escucha
 * {@link NotificationMessagingConfig#QUEUE_NAME}, completamente desacoplado
 * en el tiempo de la operación original de Booking (consistencia eventual):
 * la reserva ya fue confirmada y respondida al cliente antes de que este
 * listener siquiera se ejecute.
 *
 * Alcance de esta sesión: solo logueamos la "notificación" simulada. El
 * envío real de email/SMS/push (y la integración con un proveedor externo,
 * como aparece en el diagrama de contexto C4) está fuera del alcance del
 * módulo — lo importante acá es el mecanismo de mensajería, no el canal de
 * entrega final.
 */
@Slf4j
@Component
public class ReservationNotificationListener {

    @RabbitListener(queues = NotificationMessagingConfig.QUEUE_NAME)
    public void onReservationEvent(ReservationEventMessage message) {
        switch (message.eventType()) {
            case "RESERVATION_CONFIRMED" -> log.info(
                    "[NOTIFICATION] Enviando email de confirmación a {} — reserva {} ({} → {})",
                    message.guestEmail(), message.reservationId(),
                    message.checkIn(), message.checkOut());

            case "RESERVATION_CANCELLED" -> log.info(
                    "[NOTIFICATION] Enviando email de cancelación a {} — reserva {}",
                    message.guestEmail(), message.reservationId());

            default -> log.warn("[NOTIFICATION] Tipo de evento no reconocido: {}", message.eventType());
        }
    }
}
