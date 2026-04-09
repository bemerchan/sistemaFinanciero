package com.flypass.financial.unit.dto;

import com.flypass.financial.dto.request.CustomerRequest;
import com.flypass.financial.entity.Customer.IdentificationType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private CustomerRequest validRequest() {
        return CustomerRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .identificationType(IdentificationType.CC)
                .identificationNumber("12345")
                .email("juan@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void validRequestHasNoViolations() {
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(validRequest());
        assertThat(violations).isEmpty();
    }

    @Test
    void blankFirstNameIsInvalid() {
        CustomerRequest req = validRequest();
        req.setFirstName("");
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
    }

    @Test
    void firstNameTooShortIsInvalid() {
        CustomerRequest req = validRequest();
        req.setFirstName("J");
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
    }

    @Test
    void firstNameWithNumbersIsInvalid() {
        CustomerRequest req = validRequest();
        req.setFirstName("Juan123");
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
    }

    @Test
    void blankLastNameIsInvalid() {
        CustomerRequest req = validRequest();
        req.setLastName("   ");
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
    }

    @Test
    void nullIdentificationTypeIsInvalid() {
        CustomerRequest req = validRequest();
        req.setIdentificationType(null);
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("identificationType"));
    }

    @Test
    void blankIdentificationNumberIsInvalid() {
        CustomerRequest req = validRequest();
        req.setIdentificationNumber("");
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("identificationNumber"));
    }

    @Test
    void tooShortIdentificationNumberIsInvalid() {
        CustomerRequest req = validRequest();
        req.setIdentificationNumber("123");
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("identificationNumber"));
    }

    @Test
    void invalidEmailFormatIsInvalid() {
        CustomerRequest req = validRequest();
        req.setEmail("not-an-email");
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void blankEmailIsInvalid() {
        CustomerRequest req = validRequest();
        req.setEmail("");
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void nullBirthDateIsInvalid() {
        CustomerRequest req = validRequest();
        req.setBirthDate(null);
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("birthDate"));
    }

    @Test
    void futureBirthDateIsInvalid() {
        CustomerRequest req = validRequest();
        req.setBirthDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("birthDate"));
    }

    @Test
    void allIdentificationTypesAreAccepted() {
        for (IdentificationType type : IdentificationType.values()) {
            CustomerRequest req = validRequest();
            req.setIdentificationType(type);
            Set<ConstraintViolation<CustomerRequest>> violations = validator.validate(req);
            assertThat(violations).as("Violations for type " + type).isEmpty();
        }
    }
}
