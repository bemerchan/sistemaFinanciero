package com.flypass.financial.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flypass.financial.dto.request.TransactionRequest;
import com.flypass.financial.dto.response.TransactionResponse;
import com.flypass.financial.entity.Transaction.TransactionType;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.service.TransactionService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private TransactionResponse depositResponse;
    private TransactionResponse withdrawalResponse;

    @BeforeEach
    void setUp() {
        depositResponse = TransactionResponse.builder()
                .id(1L)
                .transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .balanceAfter(new BigDecimal("1500.00"))
                .description("Consignación inicial")
                .accountId(1L)
                .accountNumber("5300000001")
                .createdAt(LocalDateTime.now())
                .build();

        withdrawalResponse = TransactionResponse.builder()
                .id(2L)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(new BigDecimal("200.00"))
                .balanceAfter(new BigDecimal("1300.00"))
                .description("Retiro")
                .accountId(1L)
                .accountNumber("5300000001")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private TransactionRequest depositRequest() {
        return new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("500.00"),
                "Consignación inicial");
    }

    private TransactionRequest withdrawalRequest() {
        return new TransactionRequest(TransactionType.WITHDRAWAL, new BigDecimal("200.00"), "Retiro");
    }

    // ── POST /api/v1/transactions/account/{accountId} ───────────────────────────

    @Test
    void registerDeposit_returns201() throws Exception {
        when(transactionService.registerTransaction(eq(1L), any())).thenReturn(depositResponse);

        mockMvc.perform(post("/api/v1/transactions/account/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transacción registrada exitosamente"))
                .andExpect(jsonPath("$.data.transactionType").value("DEPOSIT"))
                .andExpect(jsonPath("$.data.amount").value(500.00))
                .andExpect(jsonPath("$.data.balanceAfter").value(1500.00))
                .andExpect(jsonPath("$.data.accountNumber").value("5300000001"));
    }

    @Test
    void registerWithdrawal_returns201() throws Exception {
        when(transactionService.registerTransaction(eq(1L), any())).thenReturn(withdrawalResponse);

        mockMvc.perform(post("/api/v1/transactions/account/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawalRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.transactionType").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.data.balanceAfter").value(1300.00));
    }

    @Test
    void registerTransaction_nullType_returns400() throws Exception {
        String body = """
                { "amount": 100.00 }
                """;

        mockMvc.perform(post("/api/v1/transactions/account/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void registerTransaction_nullAmount_returns400() throws Exception {
        String body = """
                { "transactionType": "DEPOSIT" }
                """;

        mockMvc.perform(post("/api/v1/transactions/account/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTransaction_zeroAmount_returns400() throws Exception {
        TransactionRequest req = new TransactionRequest(TransactionType.DEPOSIT, BigDecimal.ZERO, null);

        mockMvc.perform(post("/api/v1/transactions/account/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTransaction_negativeAmount_returns400() throws Exception {
        TransactionRequest req = new TransactionRequest(
                TransactionType.DEPOSIT, new BigDecimal("-10.00"), null);

        mockMvc.perform(post("/api/v1/transactions/account/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTransaction_accountNotFound_returns404() throws Exception {
        when(transactionService.registerTransaction(eq(999L), any()))
                .thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));

        mockMvc.perform(post("/api/v1/transactions/account/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cuenta no encontrada"));
    }

    @Test
    void registerTransaction_inactiveAccount_returns422() throws Exception {
        when(transactionService.registerTransaction(eq(1L), any()))
                .thenThrow(new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "La cuenta está inactiva y no puede recibir transacciones"));

        mockMvc.perform(post("/api/v1/transactions/account/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void registerTransaction_insufficientFunds_returns422() throws Exception {
        when(transactionService.registerTransaction(eq(1L), any()))
                .thenThrow(new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Fondos insuficientes para realizar el retiro"));

        mockMvc.perform(post("/api/v1/transactions/account/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawalRequest())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Fondos insuficientes")));
    }

    @Test
    void registerTransaction_invalidType_returns400() throws Exception {
        String body = """
                { "transactionType": "INVALID", "amount": 100.00 }
                """;

        mockMvc.perform(post("/api/v1/transactions/account/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerTransaction_withDescription_returns201() throws Exception {
        TransactionRequest req = new TransactionRequest(
                TransactionType.DEPOSIT, new BigDecimal("100.00"), "Bono de fin de año");
        depositResponse.setDescription("Bono de fin de año");
        when(transactionService.registerTransaction(eq(1L), any())).thenReturn(depositResponse);

        mockMvc.perform(post("/api/v1/transactions/account/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.description").value("Bono de fin de año"));
    }
}
