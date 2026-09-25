package com.example.banking.common;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Account has insufficient funds for this withdrawal");
    }
}