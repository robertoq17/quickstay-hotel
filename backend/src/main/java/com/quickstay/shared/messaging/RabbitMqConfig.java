package com.quickstay.shared.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración cross-cutting de mensajería (dominio "shared").
 *
 * Nota importante: el publisher (Booking) serializa
 * ReservationIntegrationEvent y el consumidor (Notification)
 * deserializa a ReservationEventMessage — son clases Java
 * DISTINTAS a propósito (ver docs/session-04-domain-events.md).
 *
 * Por eso se usa TypePrecedence.INFERRED: el converter no intenta
 * reconstruir la clase exacta del emisor (que Notification ni siquiera
 * conoce), sino que deserializa según el tipo del parámetro del método
 * 
 * @RabbitListener receptor.
 *
 *                 Esto habilita el desacoplamiento entre Bounded Contexts a
 *                 través
 *                 del broker.
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);

        converter.setTypePrecedence(
                Jackson2JavaTypeMapper.TypePrecedence.INFERRED);

        return converter;
    }
}