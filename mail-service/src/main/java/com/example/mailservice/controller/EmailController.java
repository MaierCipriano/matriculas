package com.example.mailservice.controller;

import com.example.mailservice.messaging.MessagePublisher;
import com.example.mailservice.model.EmailNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    private final MessagePublisher rabbitPublisher;

    public EmailController(MessagePublisher rabbitPublisher) {
        this.rabbitPublisher = rabbitPublisher;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendRabbit(@Validated @RequestBody EmailNotification notification) {
        rabbitPublisher.publishEmailNotification(notification);
        logger.info("RabbitMQ message published for: {}", notification.getTo());
        return ResponseEntity.ok("RabbitMQ notification published");
    }
}