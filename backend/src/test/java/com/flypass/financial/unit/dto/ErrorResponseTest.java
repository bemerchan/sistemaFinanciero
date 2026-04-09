package com.flypass.financial.unit.dto;

import com.flypass.financial.dto.response.ErrorResponse;
import com.flypass.financial.dto.response.ErrorResponse.FieldErrorDetail;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void builderCreatesErrorResponseWithAllFields() {
        FieldErrorDetail fieldError = FieldErrorDetail.builder()
                .field("email")
                .rejectedValue("bad-email")
                .message("Formato inválido")
                .build();

        ErrorResponse response = ErrorResponse.builder()
                .status(400)
                .error("Bad Request")
                .message("Error de validación")
                .errors(List.of(fieldError))
                .build();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getError()).isEqualTo("Bad Request");
        assertThat(response.getMessage()).isEqualTo("Error de validación");
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void fieldErrorDetailBuilderSetsAllFields() {
        FieldErrorDetail detail = FieldErrorDetail.builder()
                .field("firstName")
                .rejectedValue("")
                .message("El nombre es obligatorio")
                .build();

        assertThat(detail.getField()).isEqualTo("firstName");
        assertThat(detail.getRejectedValue()).isEqualTo("");
        assertThat(detail.getMessage()).isEqualTo("El nombre es obligatorio");
    }

    @Test
    void builderWithNullErrorsIsValid() {
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Recurso no encontrado")
                .build();

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void timestampIsSetByDefault() {
        ErrorResponse response = ErrorResponse.builder().status(500).build();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void fieldErrorDetailWithNullRejectedValue() {
        FieldErrorDetail detail = FieldErrorDetail.builder()
                .field("id")
                .rejectedValue(null)
                .message("Debe ser positivo")
                .build();

        assertThat(detail.getRejectedValue()).isNull();
    }

    @Test
    void fieldErrorDetailBuilderWithAllFieldsIsEqual() {
        FieldErrorDetail d1 = FieldErrorDetail.builder()
                .field("amount").rejectedValue("-5.00").message("Monto inválido").build();
        FieldErrorDetail d2 = FieldErrorDetail.builder()
                .field("amount").rejectedValue("-5.00").message("Monto inválido").build();

        assertThat(d1.getField()).isEqualTo("amount");
        assertThat(d1.getRejectedValue()).isEqualTo("-5.00");
        assertThat(d1).isEqualTo(d2);
    }
}
