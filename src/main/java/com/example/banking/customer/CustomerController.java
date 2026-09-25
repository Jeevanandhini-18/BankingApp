package com.example.banking.customer;

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
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerDtos.Response create(@Valid @RequestBody CustomerDtos.CreateRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<CustomerDtos.Response> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public CustomerDtos.Response findById(@PathVariable Long id) {
        return service.findById(id);
    }
}