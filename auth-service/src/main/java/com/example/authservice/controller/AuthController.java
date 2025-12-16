package com.example.authservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JdbcTemplate jdbcTemplate;

    public AuthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/db/ping")
    public ResponseEntity<String> pingDb() {
        Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        return ResponseEntity.ok("DB OK: " + one);
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validate(@RequestHeader("X-API-KEY") String apiKey) {
        // Si pasó el filtro, respondemos autorizado
        return ResponseEntity.ok("Authorized");
    }
}