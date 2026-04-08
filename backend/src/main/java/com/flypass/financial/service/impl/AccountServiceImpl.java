package com.flypass.financial.service.impl;

import com.flypass.financial.dto.request.AccountRequest;
import com.flypass.financial.dto.response.AccountResponse;
import com.flypass.financial.entity.Account;
import com.flypass.financial.entity.Account.AccountType;
import com.flypass.financial.entity.Customer;
import com.flypass.financial.exception.ResourceNotFoundException;
import com.flypass.financial.repository.AccountRepository;
import com.flypass.financial.repository.CustomerRepository;
import com.flypass.financial.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final String SAVINGS_PREFIX = "53";
    private static final String CHECKING_PREFIX = "33";
    private static final int ACCOUNT_NUMBER_LENGTH = 10;

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final Random random = new Random();

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        log.info("Creando cuenta para cliente ID: {}", request.getCustomerId());

        if (!customerRepository.existsById(request.getCustomerId())) {
            throw new ResourceNotFoundException("Cliente", request.getCustomerId());
        }

        String accountNumber = generateUniqueAccountNumber(request.getAccountType());

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
    public List<AccountResponse> getAccountsByCustomer(Long customerId) {
        log.info("Listando cuentas para cliente ID: {}", customerId);
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Cliente", customerId);
        }
        return accountRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        log.info("Buscando cuenta con ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta", id));
        return mapToResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountBalance(Long id) {
        log.info("Consultando saldo de cuenta ID: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta", id));
        return mapToResponse(account);
    }

    private String generateUniqueAccountNumber(AccountType accountType) {
        String prefix = accountType == AccountType.SAVINGS ? SAVINGS_PREFIX : CHECKING_PREFIX;
        String accountNumber;
        int maxAttempts = 10;
        int attempts = 0;

        do {
            int digitsNeeded = ACCOUNT_NUMBER_LENGTH - prefix.length();
            StringBuilder sb = new StringBuilder(prefix);
            for (int i = 0; i < digitsNeeded; i++) {
                sb.append(random.nextInt(10));
            }
            accountNumber = sb.toString();
            attempts++;
            if (attempts > maxAttempts) {
                throw new IllegalStateException("No se pudo generar un número de cuenta único");
            }
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
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
