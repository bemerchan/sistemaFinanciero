package com.flypass.financial.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(BigDecimal currentBalance, BigDecimal amount) {
        super(String.format(
                "Fondos insuficientes. Saldo actual: $%.2f, Monto solicitado: $%.2f",
                currentBalance, amount));
    }
}
