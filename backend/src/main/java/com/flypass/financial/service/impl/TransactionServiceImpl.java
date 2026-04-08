package com.flypass.financial.service.impl;

import com.flypass.financial.dto.request.TransactionRequest;
import com.flypass.financial.dto.response.TransactionResponse;
import com.flypass.financial.entity.Account;
import com.flypass.financial.entity.Account.AccountType;
import com.flypass.financial.entity.Transaction;
import com.flypass.financial.entity.Transaction.TransactionType;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.repository.AccountRepository;
import com.flypass.financial.repository.TransactionRepository;
import com.flypass.financial.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public TransactionResponse registerTransaction(Long accountId, TransactionRequest request) {
        log.info("Registrando transacción {} por ${} en cuenta ID: {}",
                request.getTransactionType(), request.getAmount(), accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        String.format("Cuenta con ID %d no encontrada", accountId)));

        if (account.getStatus() == Account.AccountStatus.INACTIVE) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se pueden realizar transacciones en una cuenta inactiva");
        }

        BigDecimal newBalance;
        if (request.getTransactionType() == TransactionType.DEPOSIT) {
            newBalance = account.getBalance().add(request.getAmount());
        } else {
            validateWithdrawal(account, request.getAmount());
            newBalance = account.getBalance().subtract(request.getAmount());
        }

        account.setBalance(newBalance);
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionType(request.getTransactionType())
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .description(request.getDescription())
                .accountId(accountId)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transacción registrada con ID: {}, nuevo saldo: ${}", savedTransaction.getId(), newBalance);
        return mapToResponse(savedTransaction, account);
    }

    private void validateWithdrawal(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    String.format("Fondos insuficientes. Saldo actual: $%.2f, Monto solicitado: $%.2f",
                            account.getBalance(), amount));
        }
        BigDecimal newBalance = account.getBalance().subtract(amount);
        if (account.getAccountType() == AccountType.SAVINGS
                && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Las cuentas de ahorro no pueden tener saldo negativo. Saldo mínimo: $0");
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction, Account account) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .accountId(transaction.getAccountId())
                .accountNumber(account.getAccountNumber())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
