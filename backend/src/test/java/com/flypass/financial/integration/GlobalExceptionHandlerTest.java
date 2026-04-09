package com.flypass.financial.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flypass.financial.dto.response.ErrorResponse;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.exception.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.flypass.financial.service.CustomerService customerService;

    @MockBean
    private com.flypass.financial.service.AccountService accountService;

    @MockBean
    private com.flypass.financial.service.TransactionService transactionService;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ── Direct handler unit tests ────────────────────────────────────────────────

    @Test
    void handleApiException_buildsCorrectResponse() {
        ApiException ex = new ApiException(HttpStatus.NOT_FOUND, "Recurso no encontrado");
        ResponseEntity<ErrorResponse> response = handler.handleApiException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Recurso no encontrado");
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
    }

    @Test
    void handleApiException_conflictStatus() {
        ApiException ex = new ApiException(HttpStatus.CONFLICT, "Duplicado");
        ResponseEntity<ErrorResponse> response = handler.handleApiException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    void handleApiException_unprocessableEntity() {
        ApiException ex = new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Menor de edad");
        ResponseEntity<ErrorResponse> response = handler.handleApiException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getMessage()).isEqualTo("Menor de edad");
    }

    @Test
    void handleSdrResourceNotFound_returns404() {
        org.springframework.data.rest.webmvc.ResourceNotFoundException ex =
                new org.springframework.data.rest.webmvc.ResourceNotFoundException("Not found");
        ResponseEntity<ErrorResponse> response = handler.handleSdrResourceNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void handleConstraintViolation_returns400WithDetails() {
        ConstraintViolation<?> cv = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("registerTransaction.amount");
        when(cv.getPropertyPath()).thenReturn(path);
        when(cv.getMessage()).thenReturn("El monto debe ser mayor a $0.00");
        when(cv.getInvalidValue()).thenReturn("-1");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(cv));
        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrors()).hasSize(1);
        assertThat(response.getBody().getErrors().get(0).getMessage())
                .isEqualTo("El monto debe ser mayor a $0.00");
    }

    @Test
    void handleConstraintViolation_withNullInvalidValue() {
        ConstraintViolation<?> cv = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("method.field");
        when(cv.getPropertyPath()).thenReturn(path);
        when(cv.getMessage()).thenReturn("No puede ser nulo");
        when(cv.getInvalidValue()).thenReturn(null);

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(cv));
        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrors().get(0).getRejectedValue()).isNull();
    }

    @Test
    void handleDataIntegrity_withUniqueKeyword_returns409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "unique constraint violation");
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).contains("únicos");
    }

    @Test
    void handleDataIntegrity_withoutUniqueKeyword_returns409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "foreign key constraint violation");
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("dependencias");
    }

    @Test
    void handleDataIntegrity_withNullMessage_returns409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(null);
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void handleIllegalArgument_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Argumento inválido");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Argumento inválido");
    }

    @Test
    void handleGenericException_returns500() {
        Exception ex = new RuntimeException("Error inesperado");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).contains("error interno");
    }

    @Test
    void handleMissingParam_returns400() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("page", "Integer");
        ResponseEntity<ErrorResponse> response = handler.handleMissingParam(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrors()).hasSize(1);
        assertThat(response.getBody().getErrors().get(0).getField()).isEqualTo("page");
    }

    @Test
    void handleMethodNotSupported_returns405() {
        org.springframework.web.HttpRequestMethodNotSupportedException ex =
                new org.springframework.web.HttpRequestMethodNotSupportedException("DELETE",
                        java.util.List.of("GET", "POST"));
        ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody().getMessage()).contains("DELETE");
    }

    @Test
    void handleMediaTypeNotSupported_returns415() {
        org.springframework.web.HttpMediaTypeNotSupportedException ex =
                new org.springframework.web.HttpMediaTypeNotSupportedException(
                        MediaType.TEXT_PLAIN,
                        java.util.List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<ErrorResponse> response = handler.handleMediaTypeNotSupported(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(response.getBody().getMessage()).contains("application/json");
    }

    @Test
    void handleNoHandlerFound_returns404() {
        org.springframework.web.servlet.NoHandlerFoundException ex =
                new org.springframework.web.servlet.NoHandlerFoundException(
                        "GET", "/api/v1/unknown",
                        new org.springframework.http.HttpHeaders());
        ResponseEntity<ErrorResponse> response = handler.handleNoHandlerFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).contains("/api/v1/unknown");
    }

    // ── Integration tests via MockMvc ────────────────────────────────────────────

    @Test
    void invalidJsonBody_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void wrongContentType_returns415() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text body"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void invalidEnumValue_returns400() throws Exception {
        String body = """
                {
                  "firstName": "Test",
                  "lastName": "User",
                  "identificationType": "INVALID_TYPE",
                  "identificationNumber": "12345",
                  "email": "test@test.com",
                  "birthDate": "1990-01-01"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dataIntegrityViolation_returns409() throws Exception {
        when(customerService.createCustomer(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DataIntegrityViolationException("unique constraint"));

        String body = """
                {
                  "firstName": "Juan",
                  "lastName": "Pérez",
                  "identificationType": "CC",
                  "identificationNumber": "12345",
                  "email": "juan@example.com",
                  "birthDate": "1990-01-01"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
