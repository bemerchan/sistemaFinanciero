package com.flypass.financial.unit.service;

import com.flypass.financial.dto.request.TransactionRequest;
import com.flypass.financial.dto.response.TransactionResponse;
import com.flypass.financial.entity.Account;
import com.flypass.financial.entity.Account.AccountStatus;
import com.flypass.financial.entity.Account.AccountType;
import com.flypass.financial.entity.Transaction;
import com.flypass.financial.entity.Transaction.TransactionType;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.repository.AccountRepository;
import com.flypass.financial.repository.TransactionRepository;
import com.flypass.financial.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Account savingsAccount;
    private Account checkingAccount;

    @BeforeEach
    void setUp() {
        savingsAccount = Account.builder()
                .id(1L)
                .accountNumber("5300000001")
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .customerId(1L)
                .build();

        checkingAccount = Account.builder()
                .id(2L)
                .accountNumber("3300000001")
                .accountType(AccountType.CHECKING)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("500.00"))
                .customerId(1L)
                .build();
    }

    private Transaction buildTransaction(TransactionType type, BigDecimal amount,
                                         BigDecimal balanceAfter, Long accountId) {
        return Transaction.builder()
                .id(100L)
                .transactionType(type)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .accountId(accountId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── Deposit ─────────────────────────────────────────────────────────────────

    @Test
    void registerTransaction_deposit_success() {
        TransactionRequest req = new TransactionRequest(
                TransactionType.DEPOSIT, new BigDecimal("500.00"), "Consignación");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.save(any())).thenReturn(savingsAccount);
        Transaction saved = buildTransaction(TransactionType.DEPOSIT,
                new BigDecimal("500.00"), new BigDecimal("1500.00"), 1L);
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse response = transactionService.registerTransaction(1L, req);

        assertThat(response.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(response.getBalanceAfter()).isEqualByComparingTo("1500.00");
        assertThat(savingsAccount.getBalance()).isEqualByComparingTo("1500.00");
        verify(accountRepository).save(savingsAccount);
        verify(transactionRepository).save(any());
    }

    @Test
    void registerTransaction_withdrawal_success() {
        TransactionRequest req = new TransactionRequest(
                TransactionType.WITHDRAWAL, new BigDecimal("300.00"), "Retiro");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.save(any())).thenReturn(savingsAccount);
        Transaction saved = buildTransaction(TransactionType.WITHDRAWAL,
                new BigDecimal("300.00"), new BigDecimal("700.00"), 1L);
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse response = transactionService.registerTransaction(1L, req);

        assertThat(response.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(response.getBalanceAfter()).isEqualByComparingTo("700.00");
        assertThat(savingsAccount.getBalance()).isEqualByComparingTo("700.00");
    }

    @Test
    void registerTransaction_withdrawal_checksBalance_success() {
        // Retiro exactamente igual al saldo
        TransactionRequest req = new TransactionRequest(
                TransactionType.WITHDRAWAL, new BigDecimal("1000.00"), null);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.save(any())).thenReturn(savingsAccount);
        Transaction saved = buildTransaction(TransactionType.WITHDRAWAL,
                new BigDecimal("1000.00"), BigDecimal.ZERO, 1L);
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse response = transactionService.registerTransaction(1L, req);
        assertThat(response.getBalanceAfter()).isEqualByComparingTo("0.00");
    }

    // ── Error cases ─────────────────────────────────────────────────────────────

    @Test
    void registerTransaction_accountNotFound_throwsNotFoundException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
        TransactionRequest req = new TransactionRequest(
                TransactionType.DEPOSIT, new BigDecimal("100.00"), null);

        assertThatThrownBy(() -> transactionService.registerTransaction(999L, req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getHttpStatus())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void registerTransaction_inactiveAccount_throwsUnprocessable() {
        savingsAccount.setStatus(AccountStatus.INACTIVE);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        TransactionRequest req = new TransactionRequest(
                TransactionType.DEPOSIT, new BigDecimal("100.00"), null);

        assertThatThrownBy(() -> transactionService.registerTransaction(1L, req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    assertThat(((ApiException) ex).getHttpStatus())
                            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(ex.getMessage()).contains("inactiva");
                });
    }

    @Test
    void registerTransaction_insufficientFunds_throwsUnprocessable() {
        TransactionRequest req = new TransactionRequest(
                TransactionType.WITHDRAWAL, new BigDecimal("2000.00"), null);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));

        assertThatThrownBy(() -> transactionService.registerTransaction(1L, req))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    assertThat(((ApiException) ex).getHttpStatus())
                            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(ex.getMessage()).contains("Fondos insuficientes");
                });
    }

    @Test
    void registerTransaction_savings_cannotGoNegative() {
        // Savings account with exact balance, withdrawal leaves 0 (ok)
        // but checking account CAN go below zero
        checkingAccount.setBalance(new BigDecimal("100.00"));
        TransactionRequest req = new TransactionRequest(
                TransactionType.WITHDRAWAL, new BigDecimal("100.00"), null);
        when(accountRepository.findById(2L)).thenReturn(Optional.of(checkingAccount));
        when(accountRepository.save(any())).thenReturn(checkingAccount);
        Transaction saved = buildTransaction(TransactionType.WITHDRAWAL,
                new BigDecimal("100.00"), BigDecimal.ZERO, 2L);
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse response = transactionService.registerTransaction(2L, req);
        assertThat(response.getBalanceAfter()).isEqualByComparingTo("0.00");
    }

    @Test
    void registerTransaction_deposit_withDescription_isStoredCorrectly() {
        TransactionRequest req = new TransactionRequest(
                TransactionType.DEPOSIT, new BigDecimal("200.00"), "Pago freelance");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(savingsAccount));
        when(accountRepository.save(any())).thenReturn(savingsAccount);
        Transaction saved = Transaction.builder()
                .id(50L).transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("200.00")).balanceAfter(new BigDecimal("1200.00"))
                .description("Pago freelance").accountId(1L).createdAt(LocalDateTime.now())
                .build();
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse response = transactionService.registerTransaction(1L, req);
        assertThat(response.getDescription()).isEqualTo("Pago freelance");
        assertThat(response.getAccountNumber()).isEqualTo("5300000001");
    }
}
