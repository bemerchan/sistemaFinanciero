package com.flypass.financial.service;

import com.flypass.financial.dto.request.CustomerRequest;
import com.flypass.financial.dto.response.CustomerResponse;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse updateCustomer(Long id, CustomerRequest request);
}
