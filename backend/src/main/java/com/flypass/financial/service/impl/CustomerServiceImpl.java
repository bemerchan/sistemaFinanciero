package com.flypass.financial.service.impl;

import com.flypass.financial.dto.request.CustomerRequest;
import com.flypass.financial.dto.response.CustomerResponse;
import com.flypass.financial.entity.Customer;
import com.flypass.financial.exception.ApiException;
import com.flypass.financial.repository.CustomerRepository;
import com.flypass.financial.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final int MINIMUM_AGE = 18;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        log.info("Creando cliente con identificación: {}", request.getIdentificationNumber());

        validateAge(request.getBirthDate());

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    String.format("Ya existe un cliente con el correo '%s'", request.getEmail()));
        }

        if (customerRepository.existsByIdentificationNumber(request.getIdentificationNumber())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    String.format("Ya existe un cliente con el número de identificación '%s'",
                            request.getIdentificationNumber()));
        }

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .identificationType(request.getIdentificationType())
                .identificationNumber(request.getIdentificationNumber())
                .email(request.getEmail())
                .birthDate(request.getBirthDate())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Cliente creado exitosamente con ID: {}", savedCustomer.getId());
        return mapToResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        log.info("Listando todos los clientes");
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        log.info("Buscando cliente con ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        String.format("Cliente con ID %d no encontrado", id)));
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        log.info("Actualizando cliente con ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        String.format("Cliente con ID %d no encontrado", id)));

        validateAge(request.getBirthDate());

        if (!customer.getEmail().equals(request.getEmail()) &&
                customerRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    String.format("Ya existe un cliente con el correo '%s'", request.getEmail()));
        }

        if (!customer.getIdentificationNumber().equals(request.getIdentificationNumber()) &&
                customerRepository.existsByIdentificationNumber(request.getIdentificationNumber())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    String.format("Ya existe un cliente con el número de identificación '%s'",
                            request.getIdentificationNumber()));
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setIdentificationType(request.getIdentificationType());
        customer.setIdentificationNumber(request.getIdentificationNumber());
        customer.setEmail(request.getEmail());
        customer.setBirthDate(request.getBirthDate());

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Cliente actualizado exitosamente con ID: {}", updatedCustomer.getId());
        return mapToResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("Eliminando cliente con ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        String.format("Cliente con ID %d no encontrado", id)));

        if (customerRepository.hasAccounts(id)) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se puede eliminar el cliente porque tiene cuentas bancarias vinculadas");
        }

        customerRepository.delete(customer);
        log.info("Cliente eliminado exitosamente con ID: {}", id);
    }

    private void validateAge(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < MINIMUM_AGE) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "No se permite registrar clientes menores de edad (menores de 18 años)");
        }
    }

    private CustomerResponse mapToResponse(Customer customer) {
        int age = Period.between(customer.getBirthDate(), LocalDate.now()).getYears();
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .identificationType(customer.getIdentificationType())
                .identificationNumber(customer.getIdentificationNumber())
                .email(customer.getEmail())
                .birthDate(customer.getBirthDate())
                .age(age)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
