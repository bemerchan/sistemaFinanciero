package com.flypass.financial.repository;

import com.flypass.financial.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;

@RepositoryRestResource(path = "customers", collectionResourceRel = "customers", itemResourceRel = "customer")
@Tag(name = "Clientes", description = "Gestión de clientes: POST y PUT via controlador, GET y DELETE via Spring Data REST en /api/v1/customers")
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @RestResource(exported = false)
    boolean existsByEmail(String email);

    @RestResource(exported = false)
    boolean existsByIdentificationNumber(String identificationNumber);

    @RestResource(exported = false)
    Optional<Customer> findByEmail(String email);

    @RestResource(exported = false)
    Optional<Customer> findByIdentificationNumber(String identificationNumber);

    @RestResource(exported = false)
    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.customerId = :customerId")
    boolean hasAccounts(@Param("customerId") Long customerId);
}
