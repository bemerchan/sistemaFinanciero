package com.flypass.financial.repository;

import com.flypass.financial.entity.Account;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(path = "accounts", collectionResourceRel = "accounts", itemResourceRel = "account")
public interface AccountRepository extends JpaRepository<Account, Long> {

    @RestResource(rel = "byCustomer", path = "byCustomer")
    List<Account> findByCustomerId(@Param("customerId") Long customerId);

    @RestResource(exported = false)
    boolean existsByAccountNumber(String accountNumber);

    @RestResource(exported = false)
    Optional<Account> findByAccountNumber(String accountNumber);
}
