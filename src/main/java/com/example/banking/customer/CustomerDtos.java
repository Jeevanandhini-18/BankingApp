package com.example.banking.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class CustomerDtos {
    private CustomerDtos() {}

    public record CreateRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 254) String email,
            @Size(max = 30) String phone) {
    }

    public record Response(Long id, String fullName, String email, String phone) {
        static Response from(Customer customer) {
            return new Response(customer.getId(), customer.getFullName(), customer.getEmail(), customer.getPhone());
        }
    }
}