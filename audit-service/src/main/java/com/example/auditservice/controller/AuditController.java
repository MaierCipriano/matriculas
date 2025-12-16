package com.example.auditservice.controller;

import com.example.auditservice.messaging.AuditKafkaPublisher;
import com.example.auditservice.model.AuditEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditKafkaPublisher kafkaPublisher;

    public AuditController(AuditKafkaPublisher kafkaPublisher) {
        this.kafkaPublisher = kafkaPublisher;
    }

    @PostMapping("/event")
    public ResponseEntity<String> sendKafka(@Validated @RequestBody AuditEvent event) {
        kafkaPublisher.publish(event);
        return ResponseEntity.ok("Kafka audit event published");
    }
}