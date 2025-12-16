package com.example.mailservice.messaging;

import com.example.mailservice.model.EmailNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(MessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routingkey}")
    private String routingKey;

    public MessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEmailNotification(EmailNotification notification) {
        try {
            logger.info("Sending email notification to queue: {}", notification.getTo());
            rabbitTemplate.convertAndSend(exchange, routingKey, notification);
            logger.info("Email notification sent successfully to queue");
        } catch (AmqpConnectException | IllegalStateException e) {
            logger.warn("RabbitMQ not available. Would have sent email to: {}", notification.getTo());
            logger.info("Email Subject: {}", notification.getSubject());
            logger.info("Email Body: {}", notification.getBody());
        } catch (Exception e) {
            logger.error("Error sending email notification: {}", e.getMessage(), e);
            logger.info("Email would have been sent to: {}", notification.getTo());
        }
    }
}