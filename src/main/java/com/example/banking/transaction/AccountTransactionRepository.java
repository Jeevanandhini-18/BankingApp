package com.example.banking.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
    List<AccountTransaction> findAllByAccountIdOrderByCreatedAtDesc(Long accountId);
}