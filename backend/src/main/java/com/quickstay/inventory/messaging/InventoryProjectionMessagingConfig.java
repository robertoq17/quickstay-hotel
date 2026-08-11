package com.quickstay.inventory.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Inventory owns the queue that builds its CQRS read model.
 *
 * Booking publishes integration events to the shared exchange; Inventory
 * subscribes independently and materializes the read-side projection.
 */
@Configuration
public class InventoryProjectionMessagingConfig {

    public static final String QUEUE_NAME = "inventory.room-availability-projection";

    @Bean
    public Queue inventoryProjectionQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding inventoryProjectionBinding(
            Queue inventoryProjectionQueue,
            TopicExchange reservationEventsExchange
    ) {
        return BindingBuilder.bind(inventoryProjectionQueue)
                .to(reservationEventsExchange)
                .with("reservation.*");
    }
}
