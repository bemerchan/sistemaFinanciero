package com.flypass.financial.unit.service;

import com.flypass.financial.dto.request.CustomerRequest;
import com.flypass.financial.dto.response.CustomerResponse;
import com.flypass.financial.entity.Customer;
import com.flypass.financial.entity.Customer.IdentificationType;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.repository.CustomerRepository;
import com.flypass.financial.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private CustomerRequest validRequest;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        validRequest = CustomerRequest.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .identificationType(IdentificationType.CC)
                .identificationNumber("12345678")
                .email("juan@example.com")
                .birthDate(LocalDate.of(1990, 6, 15))
                .build();

        savedCustomer = Customer.builder()
                .id(1L)
                .firstName("Juan")
                .lastName("Pérez")
                .identificationType(IdentificationType.CC)
                .identificationNumber("12345678")
                .email("juan@example.com")
                .birthDate(LocalDate.of(1990, 6, 15))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── createCustomer ──────────────────────────────────────────────────────────

    @Test
    void createCustomer_success() {
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(customerRepository.existsByIdentificationNumber(any())).thenReturn(false);
        when(customerRepository.save(any())).thenReturn(savedCustomer);

        CustomerResponse response = customerService.createCustomer(validRequest);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("Juan");
        assertThat(response.getEmail()).isEqualTo("juan@example.com");
        assertThat(response.getAge()).isGreaterThan(0);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_underAge_throwsApiException() {
        validRequest.setBirthDate(LocalDate.now().minusYears(17));

        assertThatThrownBy(() -> customerService.createCustomer(validRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("menores de edad");

        verify(customerRepository, never()).save(any());
    }

    @Test
    void createCustomer_exactlyEighteenYears_isAllowed() {
        validRequest.setBirthDate(LocalDate.now().minusYears(18));
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(customerRepository.existsByIdentificationNumber(any())).thenReturn(false);
        when(customerRepository.save(any())).thenReturn(savedCustomer);

        CustomerResponse response = customerService.createCustomer(validRequest);
        assertThat(response).isNotNull();
    }

    @Test
    void createCustomer_duplicateEmail_throwsConflict() {
        when(customerRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(validRequest))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));

        verify(customerRepository, never()).save(any());
    }

    @Test
    void createCustomer_duplicateIdentification_throwsConflict() {
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(customerRepository.existsByIdentificationNumber(validRequest.getIdentificationNumber()))
                .thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(validRequest))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    // ── updateCustomer ──────────────────────────────────────────────────────────

    @Test
    void updateCustomer_success() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(customerRepository.existsByIdentificationNumber(any())).thenReturn(false);
        when(customerRepository.save(any())).thenReturn(savedCustomer);

        CustomerResponse response = customerService.updateCustomer(1L, validRequest);
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void updateCustomer_notFound_throwsNotFoundException() {
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(999L, validRequest))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateCustomer_underAge_throwsApiException() {
        validRequest.setBirthDate(LocalDate.now().minusYears(16));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));

        assertThatThrownBy(() -> customerService.updateCustomer(1L, validRequest))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("menores de edad");
    }

    @Test
    void updateCustomer_sameEmail_doesNotCheckDuplicate() {
        // Same email as existing customer -> no conflict check needed
        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.existsByIdentificationNumber(any())).thenReturn(false);
        when(customerRepository.save(any())).thenReturn(savedCustomer);

        CustomerResponse response = customerService.updateCustomer(1L, validRequest);
        assertThat(response).isNotNull();
        // existsByEmail should NOT be called if email didn't change
        verify(customerRepository, never()).existsByEmail(any());
    }

    @Test
    void updateCustomer_differentEmailAlreadyTaken_throwsConflict() {
        String newEmail = "other@example.com";
        validRequest.setEmail(newEmail);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.existsByEmail(newEmail)).thenReturn(true);

        assertThatThrownBy(() -> customerService.updateCustomer(1L, validRequest))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void updateCustomer_sameIdNumber_doesNotCheckDuplicate() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.save(any())).thenReturn(savedCustomer);

        // Same identification number → no duplicate check
        CustomerResponse response = customerService.updateCustomer(1L, validRequest);
        assertThat(response).isNotNull();
        verify(customerRepository, never()).existsByIdentificationNumber(any());
    }

    @Test
    void updateCustomer_differentIdAlreadyTaken_throwsConflict() {
        String newId = "99999999";
        validRequest.setIdentificationNumber(newId);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.existsByEmail(any())).thenReturn(false);
        when(customerRepository.existsByIdentificationNumber(newId)).thenReturn(true);

        assertThatThrownBy(() -> customerService.updateCustomer(1L, validRequest))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }
}
