package com.example.banking.beneficiary;

import com.example.banking.common.ResourceNotFoundException;
import com.example.banking.customer.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BeneficiaryService {
    private final BeneficiaryRepository beneficiaries;
    private final CustomerRepository customers;

    public BeneficiaryService(BeneficiaryRepository beneficiaries, CustomerRepository customers) {
        this.beneficiaries = beneficiaries;
        this.customers = customers;
    }

    public BeneficiaryDtos.Response create(BeneficiaryDtos.CreateRequest request) {
        var customer = customers.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.customerId()));
        return BeneficiaryDtos.Response.from(beneficiaries.save(new Beneficiary(customer, request.name().trim(),
                request.accountNumber().trim(), request.bankName())));
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryDtos.Response> findAll() {
        return beneficiaries.findAll().stream().map(BeneficiaryDtos.Response::from).toList();
    }

    public void delete(Long id) {
        if (!beneficiaries.existsById(id)) {
            throw new ResourceNotFoundException("Beneficiary", id);
        }
        beneficiaries.deleteById(id);
    }
}