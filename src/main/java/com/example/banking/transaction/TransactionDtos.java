package com.example.banking.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public final class TransactionDtos {
    private TransactionDtos() {}

    public record CreateRequest(
            @NotNull TransactionType type,
            @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
            @Size(max = 240) String description) {
    }

    public record Response(Long id, TransactionType type, BigDecimal amount, String description, Instant createdAt) {
        static Response from(AccountTransaction transaction) {
            return new Response(transaction.getId(), transaction.getType(), transaction.getAmount(),
                    transaction.getDescription(), transaction.getCreatedAt());
        }
    }
}