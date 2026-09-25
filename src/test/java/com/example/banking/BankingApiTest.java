package com.example.banking;

import com.example.banking.account.BankAccountRepository;
import com.example.banking.beneficiary.BeneficiaryRepository;
import com.example.banking.customer.CustomerRepository;
import com.example.banking.transaction.AccountTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BankingApiTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private CustomerRepository customers;
    @Autowired private BankAccountRepository accounts;
    @Autowired private AccountTransactionRepository transactions;
    @Autowired private BeneficiaryRepository beneficiaries;

    @BeforeEach
    void clearData() {
        transactions.deleteAll();
        beneficiaries.deleteAll();
        accounts.deleteAll();
        customers.deleteAll();
    }

    @Test
    void createsCustomerAccountAndDepositThenReturnsUpdatedBalance() throws Exception {
        String customer = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Ada Lovelace","email":"ada@example.com","phone":"555-0100"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();
        long customerId = jsonId(customer);

        String account = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customerId + ",\"initialBalance\":25.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(25.0))
                .andReturn().getResponse().getContentAsString();
        long accountId = jsonId(account);

        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DEPOSIT\",\"amount\":10.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(35.0));
        mockMvc.perform(get("/api/accounts/{accountId}/transactions", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"));
    }

    @Test
    void rejectsInvalidCustomerAndReturnsFieldErrors() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"\",\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.fullName").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void rejectsWithdrawalThatWouldOverdrawAccount() throws Exception {
        String customer = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Grace Hopper\",\"email\":\"grace@example.com\"}"))
                .andReturn().getResponse().getContentAsString();
        long customerId = jsonId(customer);
        String account = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customerId + ",\"initialBalance\":5.00}"))
                .andReturn().getResponse().getContentAsString();
        long accountId = jsonId(account);

        mockMvc.perform(post("/api/accounts/{accountId}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"WITHDRAWAL\",\"amount\":10.00}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Account has insufficient funds for this withdrawal"));
    }

    @Test
    void createsListsAndDeletesBeneficiary() throws Exception {
        String customer = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Katherine Johnson\",\"email\":\"katherine@example.com\"}"))
                .andReturn().getResponse().getContentAsString();
        long customerId = jsonId(customer);

        String beneficiary = mockMvc.perform(post("/api/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customerId
                                + ",\"name\":\"Dorothy Vaughan\",\"accountNumber\":\"1234567890\",\"bankName\":\"Example Bank\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dorothy Vaughan"))
                .andReturn().getResponse().getContentAsString();
        long beneficiaryId = jsonId(beneficiary);

        mockMvc.perform(get("/api/beneficiaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(beneficiaryId));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/beneficiaries/{id}", beneficiaryId))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/beneficiaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

        private long jsonId(String json) {
                return ((Number) com.jayway.jsonpath.JsonPath.read(json, "$.id")).longValue();
        }
}