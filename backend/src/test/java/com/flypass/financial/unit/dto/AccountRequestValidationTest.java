package com.flypass.financial.unit.dto;

import com.flypass.financial.dto.request.AccountRequest;
import com.flypass.financial.entity.Account.AccountType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccountRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validSavingsRequestHasNoViolations() {
        AccountRequest req = AccountRequest.builder()
                .accountType(AccountType.SAVINGS)
                .customerId(1L)
                .build();
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void validCheckingRequestHasNoViolations() {
        AccountRequest req = AccountRequest.builder()
                .accountType(AccountType.CHECKING)
                .customerId(5L)
                .build();
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void nullAccountTypeIsInvalid() {
        AccountRequest req = AccountRequest.builder().customerId(1L).build();
        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("accountType"));
    }

    @Test
    void nullCustomerIdIsInvalid() {
        AccountRequest req = AccountRequest.builder().accountType(AccountType.SAVINGS).build();
        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("customerId"));
    }

    @Test
    void zeroCustomerIdIsInvalid() {
        AccountRequest req = AccountRequest.builder()
                .accountType(AccountType.SAVINGS)
                .customerId(0L)
                .build();
        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("customerId"));
    }

    @Test
    void negativeCustomerIdIsInvalid() {
        AccountRequest req = AccountRequest.builder()
                .accountType(AccountType.CHECKING)
                .customerId(-1L)
                .build();
        Set<ConstraintViolation<AccountRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("customerId"));
    }

    @Test
    void gettersReturnCorrectValues() {
        AccountRequest req = new AccountRequest(AccountType.SAVINGS, 10L);
        assertThat(req.getAccountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(req.getCustomerId()).isEqualTo(10L);
    }
}
