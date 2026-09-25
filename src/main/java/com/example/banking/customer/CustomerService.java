package com.example.banking.customer;

import com.example.banking.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {
    private final CustomerRepository customers;

    public CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    public CustomerDtos.Response create(CustomerDtos.CreateRequest request) {
        if (customers.existsByEmailIgnoreCase(request.email())) {
            throw new com.example.banking.common.ConflictException("A customer with this email already exists");
        }
        return CustomerDtos.Response.from(customers.save(
                new Customer(request.fullName().trim(), request.email().trim().toLowerCase(), request.phone())));
    }

    @Transactional(readOnly = true)
    public List<CustomerDtos.Response> findAll() {
        return customers.findAll().stream().map(CustomerDtos.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public CustomerDtos.Response findById(Long id) {
        return CustomerDtos.Response.from(customers.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id)));
    }
}