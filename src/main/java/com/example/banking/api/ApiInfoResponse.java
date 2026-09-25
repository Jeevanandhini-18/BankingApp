package com.example.banking.api;

public record ApiInfoResponse(
        String application,
        String version,
        String message
) {
}
