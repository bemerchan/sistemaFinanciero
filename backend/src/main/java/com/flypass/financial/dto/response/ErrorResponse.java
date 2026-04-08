package com.flypass.financial.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private List<FieldErrorDetail> errors;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldErrorDetail {
        private String field;
        private Object rejectedValue;
        private String message;
    }
}
