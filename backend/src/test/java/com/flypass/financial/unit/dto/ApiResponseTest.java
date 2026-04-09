package com.flypass.financial.unit.dto;

import com.flypass.financial.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void okWithDataOnlySetsSuccessTrue() {
        ApiResponse<String> response = ApiResponse.ok("Hello");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("Hello");
        assertThat(response.getMessage()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void okWithMessageAndDataSetsAllFields() {
        ApiResponse<Integer> response = ApiResponse.ok("Operación exitosa", 42);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Operación exitosa");
        assertThat(response.getData()).isEqualTo(42);
    }

    @Test
    void builderCreatesInstanceCorrectly() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(false)
                .message("Error")
                .data(null)
                .build();

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Error");
        assertThat(response.getData()).isNull();
    }

    @Test
    void noArgConstructorWorks() {
        ApiResponse<Object> response = new ApiResponse<>();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isNull();
    }

    @Test
    void allArgConstructorWorks() {
        ApiResponse<String> response = new ApiResponse<>(true, "msg", "data", null);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("msg");
        assertThat(response.getData()).isEqualTo("data");
    }

    @Test
    void timestampIsSetByDefault() {
        ApiResponse<Void> r = ApiResponse.ok(null);
        assertThat(r.getTimestamp()).isNotNull();
    }
}
