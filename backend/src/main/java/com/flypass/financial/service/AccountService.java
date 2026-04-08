package com.flypass.financial.service;

import com.flypass.financial.dto.request.AccountRequest;
import com.flypass.financial.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(AccountRequest request);

    List<AccountResponse> getAccountsByCustomer(Long customerId);

    AccountResponse getAccountById(Long id);

    AccountResponse getAccountBalance(Long id);
}
