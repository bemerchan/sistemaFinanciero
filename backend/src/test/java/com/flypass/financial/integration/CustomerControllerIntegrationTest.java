package com.flypass.financial.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flypass.financial.dto.request.CustomerRequest;
import com.flypass.financial.dto.response.CustomerResponse;
import com.flypass.financial.entity.Customer.IdentificationType;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.service.CustomerService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    private CustomerResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = CustomerResponse.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .identificationType(IdentificationType.CC)
                .identificationNumber("12345678")
                .email("juan@example.com")
                .birthDate(LocalDate.of(1990, 6, 15))
                .age(34)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private CustomerRequest validRequest() {
        return CustomerRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .identificationType(IdentificationType.CC)
                .identificationNumber("12345678")
                .email("juan@example.com")
                .birthDate(LocalDate.of(1990, 6, 15))
                .build();
    }

    // ── POST /api/v1/customers ──────────────────────────────────────────────────

    @Test
    void createCustomer_returns201WithBody() throws Exception {
        when(customerService.createCustomer(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cliente creado exitosamente"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("Juan"))
                .andExpect(jsonPath("$.data.email").value("juan@example.com"));
    }

    @Test
    void createCustomer_blankFirstName_returns400() throws Exception {
        CustomerRequest req = validRequest();
        req.setFirstName("");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
    }

    @Test
    void createCustomer_invalidEmail_returns400() throws Exception {
        CustomerRequest req = validRequest();
        req.setEmail("not-valid");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createCustomer_nullIdentificationType_returns400() throws Exception {
        String body = """
                {
                  "firstName": "Juan",
                  "lastName": "Pérez",
                  "identificationNumber": "12345",
                  "email": "juan@test.com",
                  "birthDate": "1990-01-01"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomer_futureBirthDate_returns400() throws Exception {
        CustomerRequest req = validRequest();
        req.setBirthDate(LocalDate.now().plusYears(1));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCustomer_underAge_returns422() throws Exception {
        when(customerService.createCustomer(any()))
                .thenThrow(new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "El cliente es menor de edad"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("El cliente es menor de edad"));
    }

    @Test
    void createCustomer_duplicateEmail_returns409() throws Exception {
        when(customerService.createCustomer(any()))
                .thenThrow(new ApiException(HttpStatus.CONFLICT,
                        "Ya existe un cliente con el correo 'juan@example.com'"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ── PUT /api/v1/customers/{id} ──────────────────────────────────────────────

    @Test
    void updateCustomer_returns200WithBody() throws Exception {
        when(customerService.updateCustomer(eq(1L), any())).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cliente actualizado exitosamente"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void updateCustomer_notFound_returns404() throws Exception {
        when(customerService.updateCustomer(eq(999L), any()))
                .thenThrow(new ApiException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        mockMvc.perform(put("/api/v1/customers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateCustomer_invalidRequest_returns400() throws Exception {
        CustomerRequest req = validRequest();
        req.setFirstName("J");  // Too short

        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void updateCustomer_duplicateIdentification_returns409() throws Exception {
        when(customerService.updateCustomer(eq(1L), any()))
                .thenThrow(new ApiException(HttpStatus.CONFLICT,
                        "Ya existe un cliente con el número de identificación '99999'"));

        mockMvc.perform(put("/api/v1/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict());
    }
}
