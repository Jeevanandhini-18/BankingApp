package com.example.banking.account;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public final class AccountDtos {
    private AccountDtos() {}

    public record CreateRequest(
            @NotNull @Positive Long customerId,
            @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal initialBalance) {
    }

    public record Response(Long id, String accountNumber, Long customerId, BigDecimal balance) {
        static Response from(BankAccount account) {
            return new Response(account.getId(), account.getAccountNumber(), account.getCustomer().getId(), account.getBalance());
        }
    }
}