package com.wealthcdio.banking.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String accountAId;
    private static String accountBId;

    @Test
    @Order(1)
    void createAccounts_withInitialBalances() throws Exception {
        accountAId = createAccount("Alice Smith", "1000.00");
        accountBId = createAccount("Bob Jones", "250.00");

        mockMvc.perform(get("/api/accounts/" + accountAId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    @Order(2)
    void deposit_increasesBalanceAndAppearsInLedger() throws Exception {
        mockMvc.perform(post("/api/accounts/" + accountAId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 150.50, "description": "Paycheck"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.balanceAfter").value(1150.50));

        mockMvc.perform(get("/api/accounts/" + accountAId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1150.50));

        mockMvc.perform(get("/api/accounts/" + accountAId + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Order(3)
    void withdraw_reducesBalance() throws Exception {
        mockMvc.perform(post("/api/accounts/" + accountBId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 50.00, "description": "ATM"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionType").value("WITHDRAWAL"));

        mockMvc.perform(get("/api/accounts/" + accountBId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200.00));
    }

    @Test
    @Order(4)
    void transfer_movesFundsBetweenAccounts() throws Exception {
        mockMvc.perform(post("/api/accounts/" + accountAId + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toAccountId": "%s", "amount": 200.00, "description": "Shared expense"}
                                """.formatted(accountBId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/accounts/" + accountAId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(950.50));

        mockMvc.perform(get("/api/accounts/" + accountBId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(400.00));
    }

    @Test
    @Order(5)
    void updateAccount_changesHolder() throws Exception {
        mockMvc.perform(put("/api/accounts/" + accountBId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountHolder": "Robert Jones"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountHolder").value("Robert Jones"));
    }

    @Test
    @Order(6)
    void withdraw_rejectsOverdraft() throws Exception {
        mockMvc.perform(post("/api/accounts/" + accountBId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 99999.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("INSUFFICIENT_FUNDS"));
    }

    @Test
    @Order(7)
    void transfer_rejectsSameAccount() throws Exception {
        mockMvc.perform(post("/api/accounts/" + accountAId + "/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"toAccountId": "%s", "amount": 10.00}
                                """.formatted(accountAId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("SAME_ACCOUNT_TRANSFER"));
    }

    @Test
    @Order(8)
    void deposit_rejectsInvalidAmount() throws Exception {
        mockMvc.perform(post("/api/accounts/" + accountAId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 0}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    void getAccount_returnsNotFoundForMissingId() throws Exception {
        mockMvc.perform(get("/api/accounts/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("ACCOUNT_NOT_FOUND"));
    }

    @Test
    @Order(10)
    void createAccount_withZeroInitialBalance() throws Exception {
        String accountId = createAccount("Zero Balance User", "0.00");

        mockMvc.perform(get("/api/accounts/" + accountId + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private String createAccount(String holder, String initialBalance) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"accountHolder": "%s", "initialBalance": %s}
                                """.formatted(holder, initialBalance)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        String id = body.get("id").asText();
        assertThat(id).isNotBlank();
        return id;
    }
}
