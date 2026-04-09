package com.flypass.financial.unit.exception;

import com.flypass.financial.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiExceptionTest {

    @Test
    void constructorSetsHttpStatusAndMessage() {
        ApiException ex = new ApiException(HttpStatus.NOT_FOUND, "Recurso no encontrado");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("Recurso no encontrado");
    }

    @Test
    void constructorWithConflictStatus() {
        ApiException ex = new ApiException(HttpStatus.CONFLICT, "Email duplicado");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getMessage()).isEqualTo("Email duplicado");
    }

    @Test
    void constructorWithUnprocessableEntityStatus() {
        ApiException ex = new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Cliente menor de edad");

        assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(ex.getMessage()).isEqualTo("Cliente menor de edad");
    }

    @Test
    void isRuntimeException() {
        ApiException ex = new ApiException(HttpStatus.BAD_REQUEST, "Bad input");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void canBeThrownAndCaught() {
        assertThatThrownBy(() -> {
            throw new ApiException(HttpStatus.FORBIDDEN, "Acceso denegado");
        })
                .isInstanceOf(ApiException.class)
                .hasMessage("Acceso denegado");
    }
}
