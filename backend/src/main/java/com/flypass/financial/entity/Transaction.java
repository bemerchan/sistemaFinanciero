package com.flypass.financial.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(length = 255)
    private String description;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL
    }
}
