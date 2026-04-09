package com.flypass.financial.integration;

import com.flypass.financial.dto.request.AccountRequest;
import com.flypass.financial.dto.request.CustomerRequest;
import com.flypass.financial.dto.request.TransactionRequest;
import com.flypass.financial.dto.response.AccountResponse;
import com.flypass.financial.dto.response.CustomerResponse;
import com.flypass.financial.dto.response.TransactionResponse;
import com.flypass.financial.entity.Account.AccountStatus;
import com.flypass.financial.entity.Account.AccountType;
import com.flypass.financial.entity.Customer.IdentificationType;
import com.flypass.financial.entity.Transaction.TransactionType;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.repository.AccountRepository;
import com.flypass.financial.repository.CustomerRepository;
import com.flypass.financial.service.AccountService;
import com.flypass.financial.service.CustomerService;
import com.flypass.financial.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for the real service layer using H2 in-memory database.
 * These tests exercise the full business logic with real repositories,
 * ensuring JaCoCo captures service branch coverage.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
class ServiceLayerIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    private static final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    private CustomerRequest buildValidCustomerRequest(String suffix) {
        return CustomerRequest.builder()
                .firstName("Test")
                .lastName("User")
                .identificationType(IdentificationType.CC)
                .identificationNumber("ID" + counter.incrementAndGet() + suffix)
                .email("test" + counter.get() + suffix + "@example.com")
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();
    }

    // ── CustomerService: createCustomer ─────────────────────────────────────────

    @Test
    void createCustomer_success_persists() {
        CustomerRequest req = buildValidCustomerRequest("A");
        CustomerResponse response = customerService.createCustomer(req);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("Test");
        assertThat(response.getEmail()).isEqualTo(req.getEmail());
        assertThat(response.getAge()).isGreaterThan(0);
        assertThat(customerRepository.existsById(response.getId())).isTrue();
    }

    @Test
    void createCustomer_underAge_throwsUnprocessable() {
        CustomerRequest req = buildValidCustomerRequest("B");
        req.setBirthDate(LocalDate.now().minusYears(17));

        assertThatThrownBy(() -> customerService.createCustomer(req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void createCustomer_exactlyEighteen_isAllowed() {
        CustomerRequest req = buildValidCustomerRequest("C");
        req.setBirthDate(LocalDate.now().minusYears(18));

        CustomerResponse response = customerService.createCustomer(req);
        assertThat(response.getAge()).isEqualTo(18);
    }

    @Test
    void createCustomer_duplicateEmail_throwsConflict() {
        CustomerRequest req1 = buildValidCustomerRequest("D");
        customerService.createCustomer(req1);

        CustomerRequest req2 = buildValidCustomerRequest("E");
        req2.setEmail(req1.getEmail()); // same email

        assertThatThrownBy(() -> customerService.createCustomer(req2))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createCustomer_duplicateIdentification_throwsConflict() {
        CustomerRequest req1 = buildValidCustomerRequest("F");
        customerService.createCustomer(req1);

        CustomerRequest req2 = buildValidCustomerRequest("G");
        req2.setIdentificationNumber(req1.getIdentificationNumber()); // same id

        assertThatThrownBy(() -> customerService.createCustomer(req2))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    // ── CustomerService: updateCustomer ─────────────────────────────────────────

    @Test
    void updateCustomer_success_updatesFields() {
        CustomerRequest req = buildValidCustomerRequest("H");
        CustomerResponse created = customerService.createCustomer(req);

        req.setFirstName("Updated");
        CustomerResponse updated = customerService.updateCustomer(created.getId(), req);

        assertThat(updated.getFirstName()).isEqualTo("Updated");
        assertThat(updated.getId()).isEqualTo(created.getId());
    }

    @Test
    void updateCustomer_notFound_throwsNotFoundException() {
        CustomerRequest req = buildValidCustomerRequest("I");

        assertThatThrownBy(() -> customerService.updateCustomer(999999L, req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateCustomer_underAge_throwsUnprocessable() {
        CustomerRequest req = buildValidCustomerRequest("J");
        CustomerResponse created = customerService.createCustomer(req);

        req.setBirthDate(LocalDate.now().minusYears(15));

        assertThatThrownBy(() -> customerService.updateCustomer(created.getId(), req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void updateCustomer_sameEmail_noConflict() {
        CustomerRequest req = buildValidCustomerRequest("K");
        CustomerResponse created = customerService.createCustomer(req);

        req.setFirstName("Changed");
        // Same email → should NOT throw conflict
        CustomerResponse updated = customerService.updateCustomer(created.getId(), req);
        assertThat(updated.getFirstName()).isEqualTo("Changed");
    }

    @Test
    void updateCustomer_differentEmailAlreadyTaken_throwsConflict() {
        CustomerRequest req1 = buildValidCustomerRequest("L");
        customerService.createCustomer(req1);

        CustomerRequest req2 = buildValidCustomerRequest("M");
        CustomerResponse c2 = customerService.createCustomer(req2);

        req2.setEmail(req1.getEmail()); // try to use req1's email

        assertThatThrownBy(() -> customerService.updateCustomer(c2.getId(), req2))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void updateCustomer_sameIdentification_noConflict() {
        CustomerRequest req = buildValidCustomerRequest("N");
        CustomerResponse created = customerService.createCustomer(req);

        req.setFirstName("AnotherName");
        // Same identification number → should NOT throw conflict
        CustomerResponse updated = customerService.updateCustomer(created.getId(), req);
        assertThat(updated.getFirstName()).isEqualTo("AnotherName");
    }

    @Test
    void updateCustomer_differentIdAlreadyTaken_throwsConflict() {
        CustomerRequest req1 = buildValidCustomerRequest("O");
        customerService.createCustomer(req1);

        CustomerRequest req2 = buildValidCustomerRequest("P");
        CustomerResponse c2 = customerService.createCustomer(req2);

        req2.setIdentificationNumber(req1.getIdentificationNumber());

        assertThatThrownBy(() -> customerService.updateCustomer(c2.getId(), req2))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    // ── AccountService: createAccount ───────────────────────────────────────────

    @Test
    void createAccount_savings_success() {
        CustomerRequest cReq = buildValidCustomerRequest("Q");
        CustomerResponse customer = customerService.createCustomer(cReq);

        AccountRequest aReq = new AccountRequest(AccountType.SAVINGS, customer.getId());
        AccountResponse account = accountService.createAccount(aReq);

        assertThat(account.getId()).isNotNull();
        assertThat(account.getAccountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(account.getAccountNumber()).startsWith("53").hasSize(10);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(account.getCustomerFullName()).isEqualTo("Test User");
    }

    @Test
    void createAccount_checking_success() {
        CustomerRequest cReq = buildValidCustomerRequest("R");
        CustomerResponse customer = customerService.createCustomer(cReq);

        AccountRequest aReq = new AccountRequest(AccountType.CHECKING, customer.getId());
        AccountResponse account = accountService.createAccount(aReq);

        assertThat(account.getAccountType()).isEqualTo(AccountType.CHECKING);
        assertThat(account.getAccountNumber()).startsWith("33").hasSize(10);
    }

    @Test
    void createAccount_customerNotFound_throwsNotFoundException() {
        AccountRequest req = new AccountRequest(AccountType.SAVINGS, 999999L);

        assertThatThrownBy(() -> accountService.createAccount(req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── AccountService: getAccountBalance ────────────────────────────────────────

    @Test
    void getAccountBalance_success_returnsBalance() {
        CustomerRequest cReq = buildValidCustomerRequest("S");
        CustomerResponse customer = customerService.createCustomer(cReq);
        AccountResponse account = accountService.createAccount(
                new AccountRequest(AccountType.SAVINGS, customer.getId()));

        AccountResponse balance = accountService.getAccountBalance(account.getId());

        assertThat(balance.getId()).isEqualTo(account.getId());
        assertThat(balance.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getAccountBalance_notFound_throwsNotFoundException() {
        assertThatThrownBy(() -> accountService.getAccountBalance(999999L))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ── TransactionService: registerTransaction ──────────────────────────────────

    @Test
    void registerTransaction_deposit_success_updatesBalance() {
        CustomerRequest cReq = buildValidCustomerRequest("T");
        CustomerResponse customer = customerService.createCustomer(cReq);
        AccountResponse account = accountService.createAccount(
                new AccountRequest(AccountType.SAVINGS, customer.getId()));

        TransactionRequest tReq = new TransactionRequest(
                TransactionType.DEPOSIT, new BigDecimal("500.00"), "Depósito");
        TransactionResponse tx = transactionService.registerTransaction(account.getId(), tReq);

        assertThat(tx.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(tx.getBalanceAfter()).isEqualByComparingTo("500.00");
        assertThat(tx.getAccountNumber()).startsWith("53");
    }

    @Test
    void registerTransaction_withdrawal_success_updatesBalance() {
        CustomerRequest cReq = buildValidCustomerRequest("U");
        CustomerResponse customer = customerService.createCustomer(cReq);
        AccountResponse account = accountService.createAccount(
                new AccountRequest(AccountType.SAVINGS, customer.getId()));

        transactionService.registerTransaction(account.getId(),
                new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("1000.00"), null));
        TransactionResponse tx = transactionService.registerTransaction(account.getId(),
                new TransactionRequest(TransactionType.WITHDRAWAL, new BigDecimal("300.00"), "Retiro"));

        assertThat(tx.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(tx.getBalanceAfter()).isEqualByComparingTo("700.00");
    }

    @Test
    void registerTransaction_accountNotFound_throwsNotFoundException() {
        TransactionRequest tReq = new TransactionRequest(
                TransactionType.DEPOSIT, new BigDecimal("100.00"), null);

        assertThatThrownBy(() -> transactionService.registerTransaction(999999L, tReq))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void registerTransaction_inactiveAccount_throwsUnprocessable() {
        CustomerRequest cReq = buildValidCustomerRequest("V");
        CustomerResponse customer = customerService.createCustomer(cReq);
        AccountResponse account = accountService.createAccount(
                new AccountRequest(AccountType.SAVINGS, customer.getId()));

        // Manually set account to INACTIVE
        accountRepository.findById(account.getId()).ifPresent(acc -> {
            acc.setStatus(AccountStatus.INACTIVE);
            accountRepository.save(acc);
        });

        TransactionRequest tReq = new TransactionRequest(
                TransactionType.DEPOSIT, new BigDecimal("100.00"), null);

        assertThatThrownBy(() -> transactionService.registerTransaction(account.getId(), tReq))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void registerTransaction_insufficientFunds_throwsUnprocessable() {
        CustomerRequest cReq = buildValidCustomerRequest("W");
        CustomerResponse customer = customerService.createCustomer(cReq);
        AccountResponse account = accountService.createAccount(
                new AccountRequest(AccountType.SAVINGS, customer.getId()));

        TransactionRequest tReq = new TransactionRequest(
                TransactionType.WITHDRAWAL, new BigDecimal("500.00"), null);

        assertThatThrownBy(() -> transactionService.registerTransaction(account.getId(), tReq))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    assertThat(((ApiException) ex).getHttpStatus())
                            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(ex.getMessage()).contains("Fondos insuficientes");
                });
    }

    @Test
    void registerTransaction_withDescription_storesDescription() {
        CustomerRequest cReq = buildValidCustomerRequest("X");
        CustomerResponse customer = customerService.createCustomer(cReq);
        AccountResponse account = accountService.createAccount(
                new AccountRequest(AccountType.CHECKING, customer.getId()));

        TransactionRequest tReq = new TransactionRequest(
                TransactionType.DEPOSIT, new BigDecimal("100.00"), "Bono anual");
        TransactionResponse tx = transactionService.registerTransaction(account.getId(), tReq);

        assertThat(tx.getDescription()).isEqualTo("Bono anual");
        assertThat(tx.getAccountId()).isEqualTo(account.getId());
        assertThat(tx.getId()).isNotNull();
    }
}
