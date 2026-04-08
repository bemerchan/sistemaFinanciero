package com.flypass.financial.service.impl;

import com.flypass.financial.dto.request.AccountRequest;
import com.flypass.financial.dto.response.AccountResponse;
import com.flypass.financial.entity.Account;
import com.flypass.financial.entity.Account.AccountType;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.repository.AccountRepository;
import com.flypass.financial.repository.CustomerRepository;
import com.flypass.financial.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final String SAVINGS_PREFIX = "53";
    private static final String CHECKING_PREFIX = "33";

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        log.info("Creando cuenta para cliente ID: {}", request.getCustomerId());

        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    String.format("Cliente con ID %d no encontrado", request.getCustomerId()));
        }

        String accountNumber = generateAccountNumber(request.getAccountType());

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .status(Account.AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .customerId(request.getCustomerId())
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Cuenta creada exitosamente: {}", savedAccount.getAccountNumber());
        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountBalance(Long id) {
        log.info("Consultando saldo de cuenta ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        String.format("Cuenta con ID %d no encontrada", id)));
        return mapToResponse(account);
    }

    @Transactional
    public String generateAccountNumber(AccountType accountType) {
        if (accountType == AccountType.SAVINGS) {
            return SAVINGS_PREFIX + String.format("%08d", accountRepository.getNextSavingsSequenceValue());
        } else {
            return CHECKING_PREFIX + String.format("%08d", accountRepository.getNextCheckingSequenceValue());
        }
    }

    private AccountResponse mapToResponse(Account account) {
        String customerFullName = customerRepository.findById(account.getCustomerId())
                .map(c -> c.getFirstName() + " " + c.getLastName())
                .orElse("Cliente no encontrado");

        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .balance(account.getBalance())
                .customerId(account.getCustomerId())
                .customerFullName(customerFullName)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
