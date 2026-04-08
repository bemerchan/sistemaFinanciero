package com.flypass.financial.repository;

import com.flypass.financial.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(path = "transactions", collectionResourceRel = "transactions", itemResourceRel = "transaction")
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @RestResource(rel = "byAccount", path = "byAccount")
    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId, Pageable pageable);

    @RestResource(exported = false)
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
