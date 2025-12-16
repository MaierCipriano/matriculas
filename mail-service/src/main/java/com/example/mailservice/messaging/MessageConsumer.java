package com.example.mailservice.messaging;

import com.example.mailservice.model.EmailNotification;
import com.example.mailservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);

    private final EmailService emailService;

    public MessageConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}", autoStartup = "${app.rabbitmq.listener.auto-startup:true}")
    public void receiveEmailNotification(EmailNotification notification) {
        logger.info("Received email notification from queue: {}", notification);
        try {
            emailService.sendEmail(notification);
            logger.info("Email processed successfully to: {}", notification.getTo());
        } catch (Exception e) {
            logger.error("Failed to process email notification: {}", e.getMessage(), e);
        }
    }
}