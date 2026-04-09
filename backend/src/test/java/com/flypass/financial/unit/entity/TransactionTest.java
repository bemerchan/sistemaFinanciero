package com.flypass.financial.unit.entity;

import com.flypass.financial.entity.Transaction;
import com.flypass.financial.entity.Transaction.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    @Test
    void builderCreatesTransactionWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Transaction tx = Transaction.builder()
                .id(1L)
                .transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("500.00"))
                .balanceAfter(new BigDecimal("1500.00"))
                .description("Pago de nómina")
                .accountId(10L)
                .createdAt(now)
                .build();

        assertThat(tx.getId()).isEqualTo(1L);
        assertThat(tx.getTransactionType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(tx.getAmount()).isEqualByComparingTo("500.00");
        assertThat(tx.getBalanceAfter()).isEqualByComparingTo("1500.00");
        assertThat(tx.getDescription()).isEqualTo("Pago de nómina");
        assertThat(tx.getAccountId()).isEqualTo(10L);
        assertThat(tx.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void noArgConstructorCreatesEmptyTransaction() {
        Transaction tx = new Transaction();
        assertThat(tx.getId()).isNull();
        assertThat(tx.getAmount()).isNull();
        assertThat(tx.getDescription()).isNull();
    }

    @Test
    void settersUpdateFields() {
        Transaction tx = new Transaction();
        tx.setId(7L);
        tx.setTransactionType(TransactionType.WITHDRAWAL);
        tx.setAmount(new BigDecimal("200.00"));
        tx.setBalanceAfter(new BigDecimal("800.00"));
        tx.setDescription("Retiro cajero");
        tx.setAccountId(5L);

        assertThat(tx.getId()).isEqualTo(7L);
        assertThat(tx.getTransactionType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(tx.getAmount()).isEqualByComparingTo("200.00");
        assertThat(tx.getDescription()).isEqualTo("Retiro cajero");
    }

    @Test
    void transactionTypeEnumValues() {
        assertThat(TransactionType.values())
                .containsExactlyInAnyOrder(TransactionType.DEPOSIT, TransactionType.WITHDRAWAL);
    }

    @Test
    void depositTypeHasCorrectName() {
        assertThat(TransactionType.DEPOSIT.name()).isEqualTo("DEPOSIT");
    }

    @Test
    void withdrawalTypeHasCorrectName() {
        assertThat(TransactionType.WITHDRAWAL.name()).isEqualTo("WITHDRAWAL");
    }

    @Test
    void equalsAndHashCodeWorkCorrectly() {
        Transaction t1 = Transaction.builder().id(1L).transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100")).balanceAfter(new BigDecimal("600")).accountId(1L).build();
        Transaction t2 = Transaction.builder().id(1L).transactionType(TransactionType.DEPOSIT)
                .amount(new BigDecimal("100")).balanceAfter(new BigDecimal("600")).accountId(1L).build();

        assertThat(t1).isEqualTo(t2);
    }
}
