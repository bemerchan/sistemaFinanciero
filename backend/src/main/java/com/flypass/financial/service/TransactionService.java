package com.flypass.financial.service;

import com.flypass.financial.dto.request.TransactionRequest;
import com.flypass.financial.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse registerTransaction(Long accountId, TransactionRequest request);

    List<TransactionResponse> getTransactionsByAccount(Long accountId);

    List<TransactionResponse> getLastTransactionsByAccount(Long accountId, int limit);
}
