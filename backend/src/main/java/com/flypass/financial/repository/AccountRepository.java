package com.flypass.financial.repository;

import com.flypass.financial.entity.Account;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(path = "accounts", collectionResourceRel = "accounts", itemResourceRel = "account")
@Tag(name = "Cuentas", description = "Gestión de cuentas bancarias: POST via controlador, GET y DELETE via Spring Data REST en /api/v1/accounts")
public interface AccountRepository extends JpaRepository<Account, Long> {

    @RestResource(rel = "byCustomer", path = "byCustomer")
    List<Account> findByCustomerId(@Param("customerId") Long customerId);

    @RestResource(exported = false)
    @Query(value = "SELECT nextval('savings_account_seq')", nativeQuery = true)
    Long getNextSavingsSequenceValue();

    @RestResource(exported = false)
    @Query(value = "SELECT nextval('checking_account_seq')", nativeQuery = true)
    Long getNextCheckingSequenceValue();
}
