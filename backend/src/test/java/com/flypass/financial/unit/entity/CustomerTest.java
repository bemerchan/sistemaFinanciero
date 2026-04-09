package com.flypass.financial.unit.entity;

import com.flypass.financial.entity.Customer;
import com.flypass.financial.entity.Customer.IdentificationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerTest {

    @Test
    void builderCreatesCustomerWithAllFields() {
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        LocalDateTime now = LocalDateTime.now();

        Customer customer = Customer.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .identificationType(IdentificationType.CC)
                .identificationNumber("12345678")
                .email("juan@example.com")
                .birthDate(birthDate)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(customer.getId()).isEqualTo(1L);
        assertThat(customer.getFirstName()).isEqualTo("Juan");
        assertThat(customer.getLastName()).isEqualTo("Pérez");
        assertThat(customer.getIdentificationType()).isEqualTo(IdentificationType.CC);
        assertThat(customer.getIdentificationNumber()).isEqualTo("12345678");
        assertThat(customer.getEmail()).isEqualTo("juan@example.com");
        assertThat(customer.getBirthDate()).isEqualTo(birthDate);
        assertThat(customer.getCreatedAt()).isEqualTo(now);
        assertThat(customer.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void noArgConstructorCreatesEmptyCustomer() {
        Customer customer = new Customer();
        assertThat(customer.getId()).isNull();
        assertThat(customer.getFirstName()).isNull();
        assertThat(customer.getEmail()).isNull();
    }

    @Test
    void settersUpdateFields() {
        Customer customer = new Customer();
        customer.setId(99L);
        customer.setFirstName("Ana");
        customer.setLastName("Gómez");
        customer.setEmail("ana@example.com");
        customer.setIdentificationType(IdentificationType.PASSPORT);

        assertThat(customer.getId()).isEqualTo(99L);
        assertThat(customer.getFirstName()).isEqualTo("Ana");
        assertThat(customer.getLastName()).isEqualTo("Gómez");
        assertThat(customer.getEmail()).isEqualTo("ana@example.com");
        assertThat(customer.getIdentificationType()).isEqualTo(IdentificationType.PASSPORT);
    }

    @Test
    void allIdentificationTypesAreValid() {
        assertThat(IdentificationType.values())
                .containsExactlyInAnyOrder(
                        IdentificationType.CC,
                        IdentificationType.CE,
                        IdentificationType.TI,
                        IdentificationType.PASSPORT,
                        IdentificationType.NIT);
    }

    @Test
    void equalsAndHashCodeWorkCorrectly() {
        LocalDate birthDate = LocalDate.of(1985, 1, 1);
        Customer c1 = Customer.builder().id(1L).firstName("Test").lastName("User")
                .email("t@t.com").birthDate(birthDate)
                .identificationType(IdentificationType.CC).identificationNumber("11111").build();
        Customer c2 = Customer.builder().id(1L).firstName("Test").lastName("User")
                .email("t@t.com").birthDate(birthDate)
                .identificationType(IdentificationType.CC).identificationNumber("11111").build();

        assertThat(c1).isEqualTo(c2);
        assertThat(c1.hashCode()).isEqualTo(c2.hashCode());
    }

    @Test
    void toStringContainsKeyFields() {
        Customer customer = Customer.builder().id(5L).firstName("Carlos").build();
        String str = customer.toString();
        assertThat(str).contains("Carlos");
    }
}
