package com.flypass.financial.unit.service;

import com.flypass.financial.entity.Customer;
import com.flypass.financial.entity.Customer.IdentificationType;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.handler.CustomerEventHandler;
import com.flypass.financial.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerEventHandlerTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerEventHandler handler;

    private Customer buildCustomer(Long id) {
        return Customer.builder()
                .id(id)
                .firstName("Test")
                .lastName("User")
                .identificationType(IdentificationType.CC)
                .identificationNumber("12345")
                .email("test@test.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void handleBeforeDelete_customerHasNoAccounts_doesNotThrow() {
        Customer customer = buildCustomer(1L);
        when(customerRepository.hasAccounts(1L)).thenReturn(false);

        // Should not throw
        handler.handleBeforeDelete(customer);
    }

    @Test
    void handleBeforeDelete_customerHasAccounts_throwsUnprocessable() {
        Customer customer = buildCustomer(2L);
        when(customerRepository.hasAccounts(2L)).thenReturn(true);

        assertThatThrownBy(() -> handler.handleBeforeDelete(customer))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    assertThat(((ApiException) ex).getHttpStatus())
                            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(ex.getMessage()).contains("cuentas bancarias");
                });
    }
}
