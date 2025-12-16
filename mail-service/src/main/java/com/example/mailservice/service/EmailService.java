package com.example.mailservice.service;

import com.example.mailservice.model.EmailNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(EmailNotification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getTo());
            message.setSubject(notification.getSubject());
            message.setText(notification.getBody());

            mailSender.send(message);
            logger.info("SMTP email sent to: {}", notification.getTo());
        } catch (Exception e) {
            logger.error("Failed to send SMTP email: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}