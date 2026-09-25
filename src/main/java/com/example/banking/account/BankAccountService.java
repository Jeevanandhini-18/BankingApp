package com.example.banking.account;

import com.example.banking.common.ResourceNotFoundException;
import com.example.banking.customer.Customer;
import com.example.banking.customer.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BankAccountService {
    private final BankAccountRepository accounts;
    private final CustomerRepository customers;

    public BankAccountService(BankAccountRepository accounts, CustomerRepository customers) {
        this.accounts = accounts;
        this.customers = customers;
    }

    public AccountDtos.Response create(AccountDtos.CreateRequest request) {
        Customer customer = customers.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        String accountNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
        return AccountDtos.Response.from(accounts.save(
                new BankAccount(accountNumber, customer, request.initialBalance())));
    }

    @Transactional(readOnly = true)
    public List<AccountDtos.Response> findAll() {
        return accounts.findAll().stream().map(AccountDtos.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public AccountDtos.Response findById(Long id) {
        return AccountDtos.Response.from(accounts.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", id)));
    }
}