package com.quickstay.booking.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Booking, como dueño del evento, declara el exchange. Esto sigue la
 * convención habitual en arquitecturas orientadas a eventos: el publisher
 * posee el "topic" (exchange); cada consumidor posee su propia cola y
 * decide a qué routing keys suscribirse (ver
 * notification.messaging.NotificationMessagingConfig).
 */
@Configuration
public class BookingMessagingConfig {

    public static final String RESERVATION_EXCHANGE = "quickstay.reservation-events";
    public static final String ROUTING_KEY_CONFIRMED = "reservation.confirmed";
    public static final String ROUTING_KEY_PENDING_PAYMENT = "reservation.pending-payment";
    public static final String ROUTING_KEY_CANCELLED = "reservation.cancelled";

    @Bean
    public TopicExchange reservationEventsExchange() {
        return new TopicExchange(RESERVATION_EXCHANGE, true, false);
    }
}
