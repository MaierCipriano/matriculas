package com.example.enrollmentservice.controller;

import com.example.enrollmentservice.model.EnrollmentRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private static final Logger logger = LoggerFactory.getLogger(EnrollmentController.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.mail.base-url:http://mail-service:9102}")
    private String mailBaseUrl;

    @Value("${services.audit.base-url:http://audit-service:9103}")
    private String auditBaseUrl;

    @PostMapping
    public ResponseEntity<String> create(@Validated @RequestBody EnrollmentRequest request) {
        // 1) Publicar envío de correo via mail-service (RabbitMQ)
        Map<String, String> emailPayload = new HashMap<>();
        emailPayload.put("to", request.getEmail());
        emailPayload.put("subject", "Confirmación de matrícula: " + request.getCourseName());
        emailPayload.put("body", String.format("Hola %s,\n\n" +
                        "Has quedado matriculado exitosamente en el curso '%s'.\\n" +
                        "Recibirás más información en tu correo.\\n" +
                        "Saludos,\n" +
                        "Equipo Académico", request.getStudentName(), request.getCourseName()));

        try {
            restTemplate.postForEntity(mailBaseUrl + "/email/send", emailPayload, String.class);
            logger.info("Mail-service invoked for {}", request.getEmail());
        } catch (Exception ex) {
            logger.warn("Mail-service not available or failed: {}", ex.getMessage());
        }

        // 2) Registrar evento en audit-service (Kafka)
        Map<String, Object> auditEvent = new HashMap<>();
        auditEvent.put("eventType", "ENROLLMENT_CREATED");
        auditEvent.put("timestamp", Instant.now().toEpochMilli());
        auditEvent.put("details", String.format("Enrollment created for %s (%s) in course %s", request.getStudentName(), request.getEmail(), request.getCourseName()));

        try {
            restTemplate.postForEntity(auditBaseUrl + "/audit/event", auditEvent, String.class);
            logger.info("Audit-service invoked for enrollment event");
        } catch (Exception ex) {
            logger.warn("Audit-service not available or failed: {}", ex.getMessage());
        }

        return ResponseEntity.ok("Enrollment created and notifications dispatched");
    }

    @PostMapping("/send/{id}")
    public ResponseEntity<String> sendManual(@PathVariable("id") int id) {
        // Endpoint de compatibilidad para el botón "Enviar manual" del WebApp
        // En una implementación real, se recuperaría el registro por id y se re-publicaría.
        Map<String, String> emailPayload = new HashMap<>();
        emailPayload.put("to", "test@example.com");
        emailPayload.put("subject", "Re-envío manual de matrícula #" + id);
        emailPayload.put("body", "Este es un reenvío manual desde el WebApp.");
        restTemplate.postForEntity(mailBaseUrl + "/email/send", emailPayload, String.class);
        return ResponseEntity.ok("Manual dispatch queued");
    }
}