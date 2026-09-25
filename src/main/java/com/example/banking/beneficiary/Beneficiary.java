package com.example.banking.beneficiary;

import com.example.banking.customer.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "beneficiaries")
public class Beneficiary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 34)
    private String accountNumber;

    @Column(length = 120)
    private String bankName;

    protected Beneficiary() {
    }

    public Beneficiary(Customer customer, String name, String accountNumber, String bankName) {
        this.customer = customer;
        this.name = name;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public String getName() { return name; }
    public String getAccountNumber() { return accountNumber; }
    public String getBankName() { return bankName; }
}