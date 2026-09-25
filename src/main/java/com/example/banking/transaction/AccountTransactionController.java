package com.example.banking.transaction;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
public class AccountTransactionController {
    private final AccountTransactionService service;

    public AccountTransactionController(AccountTransactionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionDtos.Response create(@PathVariable Long accountId,
                                           @Valid @RequestBody TransactionDtos.CreateRequest request) {
        return service.create(accountId, request);
    }

    @GetMapping
    public List<TransactionDtos.Response> findAll(@PathVariable Long accountId) {
        return service.findAll(accountId);
    }
}