package com.flypass.financial.unit.service;

import com.flypass.financial.dto.request.AccountRequest;
import com.flypass.financial.dto.response.AccountResponse;
import com.flypass.financial.entity.Account;
import com.flypass.financial.entity.Account.AccountStatus;
import com.flypass.financial.entity.Account.AccountType;
import com.flypass.financial.entity.Customer;
import com.flypass.financial.entity.Customer.IdentificationType;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.repository.AccountRepository;
import com.flypass.financial.repository.CustomerRepository;
import com.flypass.financial.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Customer customer;
    private Account savingsAccount;
    private Account checkingAccount;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .firstName("María")
                .lastName("López")
                .identificationType(IdentificationType.CC)
                .identificationNumber("11111111")
                .email("maria@test.com")
                .birthDate(LocalDate.of(1985, 3, 20))
                .build();

        savingsAccount = Account.builder()
                .id(1L)
                .accountNumber("5300000001")
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .customerId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        checkingAccount = Account.builder()
                .id(2L)
                .accountNumber("3300000001")
                .accountType(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .customerId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ── createAccount ───────────────────────────────────────────────────────────

    @Test
    void createAccount_savings_success() {
        AccountRequest req = new AccountRequest(AccountType.SAVINGS, 1L);
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.getNextSavingsSequenceValue()).thenReturn(1L);
        when(accountRepository.save(any())).thenReturn(savingsAccount);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        AccountResponse response = accountService.createAccount(req);

        assertThat(response.getAccountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(response.getAccountNumber()).startsWith("53");
        assertThat(response.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getCustomerFullName()).isEqualTo("María López");
    }

    @Test
    void createAccount_checking_success() {
        AccountRequest req = new AccountRequest(AccountType.CHECKING, 1L);
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.getNextCheckingSequenceValue()).thenReturn(1L);
        when(accountRepository.save(any())).thenReturn(checkingAccount);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        AccountResponse response = accountService.createAccount(req);

        assertThat(response.getAccountType()).isEqualTo(AccountType.CHECKING);
        assertThat(response.getAccountNumber()).startsWith("33");
    }

    @Test
    void createAccount_customerNotFound_throwsNotFoundException() {
        AccountRequest req = new AccountRequest(AccountType.SAVINGS, 999L);
        when(customerRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> accountService.createAccount(req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_customerNotFoundInMap_returnsDefaultName() {
        AccountRequest req = new AccountRequest(AccountType.SAVINGS, 1L);
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.getNextSavingsSequenceValue()).thenReturn(1L);
        when(accountRepository.save(any())).thenReturn(savingsAccount);
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        AccountResponse response = accountService.createAccount(req);
        assertThat(response.getCustomerFullName()).isEqualTo("Cliente no encontrado");
    }

    // ── generateAccountNumber ───────────────────────────────────────────────────

    @Test
    void generateAccountNumber_savings_startsWith53() {
        when(accountRepository.getNextSavingsSequenceValue()).thenReturn(42L);
        String number = accountService.generateAccountNumber(AccountType.SAVINGS);
        assertThat(number).startsWith("53").hasSize(10);
    }

    @Test
    void generateAccountNumber_checking_startsWith33() {
        when(accountRepository.getNextCheckingSequenceValue()).thenReturn(7L);
        String number = accountService.generateAccountNumber(AccountType.CHECKING);
        assertThat(number).startsWith("33").hasSize(10);
    }

    // ── getAccountBalance ───────────────────────────────────────────────────────

    @Test
    void getAccountBalance_success() {
        savingsAccount.setBalance(new BigDecimal("1950.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        AccountResponse response = accountService.getAccountBalance(1L);

        assertThat(response.getBalance()).isEqualByComparingTo("1950.00");
        assertThat(response.getAccountNumber()).isEqualTo("5300000001");
    }

    @Test
    void getAccountBalance_notFound_throwsNotFoundException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountBalance(999L))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
