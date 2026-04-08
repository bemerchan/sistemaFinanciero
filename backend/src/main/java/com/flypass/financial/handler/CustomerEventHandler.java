package com.flypass.financial.handler;

import com.flypass.financial.entity.Customer;
import com.flypass.financial.exception.BusinessException;
import com.flypass.financial.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
@RequiredArgsConstructor
public class CustomerEventHandler {

    private final CustomerRepository customerRepository;

    @HandleBeforeDelete
    public void handleBeforeDelete(Customer customer) {
        if (customerRepository.hasAccounts(customer.getId())) {
            throw new BusinessException(
                "No se puede eliminar el cliente porque tiene cuentas bancarias asociadas. " +
                "Elimine primero las cuentas del cliente."
            );
        }
    }
}
