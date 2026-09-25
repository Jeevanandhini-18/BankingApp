package com.example.banking.transaction;

import com.example.banking.account.BankAccount;
import com.example.banking.account.BankAccountRepository;
import com.example.banking.common.InsufficientFundsException;
import com.example.banking.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AccountTransactionService {
    private final BankAccountRepository accounts;
    private final AccountTransactionRepository transactions;

    public AccountTransactionService(BankAccountRepository accounts, AccountTransactionRepository transactions) {
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public TransactionDtos.Response create(Long accountId, TransactionDtos.CreateRequest request) {
        BankAccount account = accounts.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
        var newBalance = request.type() == TransactionType.DEPOSIT
                ? account.getBalance().add(request.amount())
                : account.getBalance().subtract(request.amount());
        if (newBalance.signum() < 0) {
            throw new InsufficientFundsException();
        }
        account.setBalance(newBalance);
        return TransactionDtos.Response.from(transactions.save(
                new AccountTransaction(account, request.type(), request.amount(), request.description())));
    }

    @Transactional(readOnly = true)
    public List<TransactionDtos.Response> findAll(Long accountId) {
        if (!accounts.existsById(accountId)) {
            throw new ResourceNotFoundException("Account", accountId);
        }
        return transactions.findAllByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(TransactionDtos.Response::from).toList();
    }
}