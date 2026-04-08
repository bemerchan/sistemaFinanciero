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

    @NotNull(message = "El tipo de cuenta es requerido (SAVINGS o CHECKING)")
    private AccountType accountType;

    @NotNull(message = "El ID del cliente es requerido")
    @Positive(message = "El ID del cliente debe ser un número positivo")
    private Long customerId;
}
