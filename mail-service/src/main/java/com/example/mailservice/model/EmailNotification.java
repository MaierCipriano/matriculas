package com.example.mailservice.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class EmailNotification {
    @Email
    @NotBlank
    private String to;
    @NotBlank
    private String subject;
    @NotBlank
    private String body;

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}