package com.example.mailservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;

@Component
public class RabbitInfrastructureInitializer {

    private static final Logger logger = LoggerFactory.getLogger(RabbitInfrastructureInitializer.class);

    private final RabbitAdmin rabbitAdmin;
    private final Queue queue;
    private final TopicExchange exchange;
    private final Binding binding;
    private final RabbitListenerEndpointRegistry listenerRegistry;

    public RabbitInfrastructureInitializer(RabbitAdmin rabbitAdmin,
                                          Queue queue,
                                          TopicExchange exchange,
                                          Binding binding,
                                          RabbitListenerEndpointRegistry listenerRegistry) {
        this.rabbitAdmin = rabbitAdmin;
        this.queue = queue;
        this.exchange = exchange;
        this.binding = binding;
        this.listenerRegistry = listenerRegistry;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void declareWithRetry() {
        int attempts = 0;
        int maxAttempts = 40; // ~120s con 3s de espera
        long sleepMillis = 3000;

        while (attempts < maxAttempts) {
            attempts++;
            try {
                // touch broker to confirm connection
                rabbitAdmin.getQueueProperties(queue.getName());
                // declare infra once broker responds
                rabbitAdmin.declareExchange(exchange);
                rabbitAdmin.declareQueue(queue);
                rabbitAdmin.declareBinding(binding);
                logger.info("RabbitMQ infrastructure declared: exchange={}, queue={}, binding created",
                        exchange.getName(), queue.getName());
                
                // Give a small grace period for the broker to stabilize permissions/connections
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }

                try {
                    // Start listeners only after the broker is reachable and infrastructure is declared
                    listenerRegistry.start();
                    logger.info("Rabbit listeners started successfully after broker became ready.");
                } catch (Exception startEx) {
                    logger.warn("Could not start rabbit listeners yet: {}", startEx.getMessage());
                }
                return;
            } catch (Exception e) {
                logger.warn("RabbitMQ not ready yet (attempt {}/{}): {}", attempts, maxAttempts, e.getMessage());
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        logger.error("Failed to declare RabbitMQ infrastructure after {} attempts; will rely on auto-declare when broker becomes available.", maxAttempts);
    }
}