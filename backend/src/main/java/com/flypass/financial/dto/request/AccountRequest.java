package com.flypass.financial.dto.request;

import com.flypass.financial.entity.Account.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @NotNull(message = "El tipo de cuenta es obligatorio. Valores válidos: SAVINGS (ahorro), CHECKING (corriente)")
    private AccountType accountType;

    @NotNull(message = "El ID del cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser un número entero positivo mayor a 0")
    private Long customerId;
}
