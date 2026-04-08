package com.flypass.financial.exception;

public class UnderageCustomerException extends RuntimeException {

    public UnderageCustomerException() {
        super("No se permite registrar clientes menores de edad (menores de 18 años)");
    }
}
