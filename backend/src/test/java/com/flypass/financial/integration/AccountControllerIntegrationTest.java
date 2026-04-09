package com.flypass.financial.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flypass.financial.dto.request.AccountRequest;
import com.flypass.financial.dto.response.AccountResponse;
import com.flypass.financial.entity.Account.AccountStatus;
import com.flypass.financial.entity.Account.AccountType;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private AccountResponse savingsResponse;
    private AccountResponse checkingResponse;

    @BeforeEach
    void setUp() {
        savingsResponse = AccountResponse.builder()
                .id(1L)
                .accountNumber("5300000001")
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .customerId(1L)
                .customerFullName("María López")
                .createdAt(LocalDateTime.now())
                .build();

        checkingResponse = AccountResponse.builder()
                .id(2L)
                .accountNumber("3300000001")
                .accountType(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .customerId(1L)
                .customerFullName("María López")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── POST /api/v1/accounts ───────────────────────────────────────────────────

    @Test
    void createAccount_savings_returns201() throws Exception {
        when(accountService.createAccount(any())).thenReturn(savingsResponse);
        AccountRequest req = new AccountRequest(AccountType.SAVINGS, 1L);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cuenta creada exitosamente"))
                .andExpect(jsonPath("$.data.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.data.accountNumber").value("5300000001"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void createAccount_checking_returns201() throws Exception {
        when(accountService.createAccount(any())).thenReturn(checkingResponse);
        AccountRequest req = new AccountRequest(AccountType.CHECKING, 1L);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accountType").value("CHECKING"))
                .andExpect(jsonPath("$.data.accountNumber").value("3300000001"));
    }

    @Test
    void createAccount_nullAccountType_returns400() throws Exception {
        String body = """
                { "customerId": 1 }
                """;

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void createAccount_nullCustomerId_returns400() throws Exception {
        String body = """
                { "accountType": "SAVINGS" }
                """;

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_invalidCustomerId_returns400() throws Exception {
        AccountRequest req = new AccountRequest(AccountType.SAVINGS, 0L);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_customerNotFound_returns404() throws Exception {
        when(accountService.createAccount(any()))
                .thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        AccountRequest req = new AccountRequest(AccountType.SAVINGS, 999L);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cliente no encontrado"));
    }

    @Test
    void createAccount_invalidAccountType_returns400() throws Exception {
        String body = """
                { "accountType": "INVALID", "customerId": 1 }
                """;

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/v1/accounts/{id}/balance ───────────────────────────────────────

    @Test
    void getBalance_returns200WithBalance() throws Exception {
        savingsResponse.setBalance(new BigDecimal("1950.00"));
        when(accountService.getAccountBalance(eq(1L))).thenReturn(savingsResponse);

        mockMvc.perform(get("/api/v1/accounts/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(1950.00))
                .andExpect(jsonPath("$.data.accountNumber").value("5300000001"));
    }

    @Test
    void getBalance_accountNotFound_returns404() throws Exception {
        when(accountService.getAccountBalance(eq(999L)))
                .thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));

        mockMvc.perform(get("/api/v1/accounts/999/balance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getBalance_checkingAccount_returns200() throws Exception {
        checkingResponse.setBalance(new BigDecimal("250.75"));
        when(accountService.getAccountBalance(eq(2L))).thenReturn(checkingResponse);

        mockMvc.perform(get("/api/v1/accounts/2/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accountType").value("CHECKING"))
                .andExpect(jsonPath("$.data.balance").value(250.75));
    }
}
