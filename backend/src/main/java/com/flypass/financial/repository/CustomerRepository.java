package com.flypass.financial.repository;

import com.flypass.financial.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    boolean existsByIdentificationNumber(String identificationNumber);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByIdentificationNumber(String identificationNumber);

    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE a.customer.id = :customerId")
    boolean hasAccounts(Long customerId);
}
