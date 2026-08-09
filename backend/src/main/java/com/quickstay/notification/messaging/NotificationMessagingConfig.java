package com.quickstay.notification.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Notification, como consumidor, declara y posee su propia cola —
 * convención estándar en topologías pub/sub: el publisher no sabe (ni le
 * importa) cuántos consumidores escuchan su exchange.
 *
 * Nota: el binding usa el routing key pattern "reservation.*" como string
 * literal (no importa las constantes de {@code booking.messaging}) — así,
 * Notification queda desacoplado de Booking incluso a nivel de código: solo
 * necesita saber el contrato de nombres del exchange/routing-keys, que es
 * la única "interfaz pública" real entre ambos Bounded Contexts.
 */
@Configuration
public class NotificationMessagingConfig {

    public static final String QUEUE_NAME = "notification.reservation-events";

    @Bean
    public Queue reservationNotificationQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding reservationNotificationBinding(
            Queue reservationNotificationQueue,
            TopicExchange reservationEventsExchange
    ) {
        // "reservation.*" cubre reservation.confirmed y reservation.cancelled
        return BindingBuilder.bind(reservationNotificationQueue)
                .to(reservationEventsExchange)
                .with("reservation.*");
    }
}
