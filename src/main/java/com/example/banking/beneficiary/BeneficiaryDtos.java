package com.example.banking.beneficiary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class BeneficiaryDtos {
    private BeneficiaryDtos() {}

    public record CreateRequest(
            @Positive long customerId,
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 34) String accountNumber,
            @Size(max = 120) String bankName) {
    }

    public record Response(Long id, Long customerId, String name, String accountNumber, String bankName) {
        static Response from(Beneficiary beneficiary) {
            return new Response(beneficiary.getId(), beneficiary.getCustomer().getId(), beneficiary.getName(),
                    beneficiary.getAccountNumber(), beneficiary.getBankName());
        }
    }
}