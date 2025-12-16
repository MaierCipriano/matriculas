package com.example.auditservice.model;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AuditEvent {
    @NotBlank
    private String eventType;
    @NotNull
    private Long timestamp;
    @NotBlank
    private String details;

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}