package com.flypass.financial.service;

import com.flypass.financial.dto.request.TransactionRequest;
import com.flypass.financial.dto.response.TransactionResponse;

public interface TransactionService {

    TransactionResponse registerTransaction(Long accountId, TransactionRequest request);
}
