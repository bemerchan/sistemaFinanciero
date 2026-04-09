package com.flypass.financial.unit.dto;

import com.flypass.financial.dto.request.TransactionRequest;
import com.flypass.financial.entity.Transaction.TransactionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private TransactionRequest validRequest() {
        return TransactionRequest.builder()
                .transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100.00"))
                .description("Depósito inicial")
                .build();
    }

    @Test
    void validDepositRequestHasNoViolations() {
        assertThat(validator.validate(validRequest())).isEmpty();
    }

    @Test
    void validWithdrawalRequestHasNoViolations() {
        TransactionRequest req = validRequest();
        req.setTransactionType(TransactionType.WITHDRAWAL);
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void nullTransactionTypeIsInvalid() {
        TransactionRequest req = validRequest();
        req.setTransactionType(null);
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("transactionType"));
    }

    @Test
    void nullAmountIsInvalid() {
        TransactionRequest req = validRequest();
        req.setAmount(null);
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
    }

    @Test
    void zeroAmountIsInvalid() {
        TransactionRequest req = validRequest();
        req.setAmount(BigDecimal.ZERO);
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
    }

    @Test
    void negativeAmountIsInvalid() {
        TransactionRequest req = validRequest();
        req.setAmount(new BigDecimal("-0.01"));
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
    }

    @Test
    void minimumValidAmountIsOnecentimo() {
        TransactionRequest req = validRequest();
        req.setAmount(new BigDecimal("0.01"));
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void descriptionOver255CharsIsInvalid() {
        TransactionRequest req = validRequest();
        req.setDescription("A".repeat(256));
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("description"));
    }

    @Test
    void nullDescriptionIsValid() {
        TransactionRequest req = validRequest();
        req.setDescription(null);
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void exactly255CharsDescriptionIsValid() {
        TransactionRequest req = validRequest();
        req.setDescription("B".repeat(255));
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void gettersReturnCorrectValues() {
        TransactionRequest req = new TransactionRequest(
                TransactionType.WITHDRAWAL, new BigDecimal("50.00"), "Retiro");
        assertThat(req.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(req.getAmount()).isEqualByComparingTo("50.00");
        assertThat(req.getDescription()).isEqualTo("Retiro");
    }
}
