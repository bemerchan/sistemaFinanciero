package com.flypass.financial.service.impl;

import com.flypass.financial.dto.request.TransactionRequest;
import com.flypass.financial.dto.response.TransactionResponse;
import com.flypass.financial.entity.Account;
import com.flypass.financial.entity.Account.AccountType;
import com.flypass.financial.entity.Transaction;
import com.flypass.financial.entity.Transaction.TransactionType;
import com.flypass.financial.exception.BusinessException;
import com.flypass.financial.exception.InsufficientFundsException;
import com.flypass.financial.exception.ResourceNotFoundException;
import com.flypass.financial.repository.AccountRepository;
import com.flypass.financial.repository.TransactionRepository;
import com.flypass.financial.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta", accountId));

        if (account.getStatus() == Account.AccountStatus.INACTIVE) {
            throw new BusinessException("No se pueden realizar transacciones en una cuenta inactiva");
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
                .account(account)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transacción registrada con ID: {}, nuevo saldo: ${}", savedTransaction.getId(), newBalance);
        return mapToResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccount(Long accountId) {
        log.info("Listando transacciones de cuenta ID: {}", accountId);

        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Cuenta", accountId);
        }

        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getLastTransactionsByAccount(Long accountId, int limit) {
        log.info("Listando últimas {} transacciones de cuenta ID: {}", limit, accountId);

        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Cuenta", accountId);
        }

        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateWithdrawal(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(account.getBalance(), amount);
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        if (account.getAccountType() == AccountType.SAVINGS
                && newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(
                    "Las cuentas de ahorro no pueden tener saldo negativo. Saldo mínimo: $0");
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        Account account = transaction.getAccount();
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
