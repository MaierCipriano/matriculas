package com.example.auditservice.messaging;

import com.example.auditservice.model.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditKafkaPublisher {

    private static final Logger logger = LoggerFactory.getLogger(AuditKafkaPublisher.class);

    private final KafkaTemplate<String, AuditEvent> kafkaTemplate;

    @Value("${app.kafka.audit-topic}")
    private String topic;

    public AuditKafkaPublisher(KafkaTemplate<String, AuditEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(AuditEvent event) {
        try {
            logger.info("Publishing audit event to Kafka topic {}: {}", topic, event.getEventType());
            kafkaTemplate.send(topic, event);
        } catch (Exception e) {
            logger.error("Error publishing audit event to Kafka: {}", e.getMessage(), e);
        }
    }
}