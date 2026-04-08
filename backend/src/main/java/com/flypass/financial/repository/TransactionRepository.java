package com.flypass.financial.repository;

import com.flypass.financial.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(path = "transactions", collectionResourceRel = "transactions", itemResourceRel = "transaction")
@Tag(name = "Transacciones", description = "Registro de transacciones: POST via controlador, GET via Spring Data REST en /api/v1/transactions")
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @RestResource(rel = "byAccount", path = "byAccount")
    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId, Pageable pageable);

    @RestResource(exported = false)
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
