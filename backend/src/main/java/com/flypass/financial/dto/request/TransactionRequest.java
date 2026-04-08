package com.flypass.financial.dto.request;

import com.flypass.financial.entity.Transaction.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "El tipo de transacción es obligatorio. Valores válidos: DEPOSIT (consignación), WITHDRAWAL (retiro)")
    private TransactionType transactionType;

    @NotNull(message = "El monto de la transacción es obligatorio")
    @DecimalMin(value = "0.01", inclusive = true,
            message = "El monto debe ser mayor a $0.00 (mínimo $0.01)")
    @DecimalMax(value = "999999999999999.99", inclusive = true,
            message = "El monto no puede exceder $999,999,999,999,999.99")
    @Digits(integer = 15, fraction = 2,
            message = "El monto debe tener máximo 15 dígitos enteros y 2 decimales")
    private BigDecimal amount;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String description;
}
