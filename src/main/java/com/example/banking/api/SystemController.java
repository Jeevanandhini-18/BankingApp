package com.example.banking.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class SystemController {

    private final JdbcTemplate jdbcTemplate;

    public SystemController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public HealthResponse health() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return new HealthResponse("UP", "UP");
        } catch (RuntimeException exception) {
            return new HealthResponse("DOWN", "DOWN");
        }
    }

    @GetMapping("/api/info")
    public ApiInfoResponse info() {
        return new ApiInfoResponse(
                "Mini Banking API",
                "0.0.1-SNAPSHOT",
                "Week 1 banking service is running"
        );
    }
}
