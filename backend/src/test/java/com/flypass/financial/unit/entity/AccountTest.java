package com.flypass.financial.unit.entity;

import com.flypass.financial.entity.Account;
import com.flypass.financial.entity.Account.AccountStatus;
import com.flypass.financial.entity.Account.AccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {

    @Test
    void builderCreatesAccountWithAllFields() {
        LocalDateTime now = LocalDateTime.now();
        Account account = Account.builder()
                .id(1L)
                .accountNumber("5300000001")
                .accountType(AccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("1500.50"))
                .customerId(10L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(account.getId()).isEqualTo(1L);
        assertThat(account.getAccountNumber()).isEqualTo("5300000001");
        assertThat(account.getAccountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getBalance()).isEqualByComparingTo("1500.50");
        assertThat(account.getCustomerId()).isEqualTo(10L);
    }

    @Test
    void defaultStatusIsActive() {
        Account account = Account.builder()
                .accountNumber("3300000001")
                .accountType(AccountType.CHECKING)
                .customerId(1L)
                .build();

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void defaultBalanceIsZero() {
        Account account = Account.builder()
                .accountNumber("5300000002")
                .accountType(AccountType.SAVINGS)
                .customerId(1L)
                .build();

        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void noArgConstructorCreatesEmptyAccount() {
        Account account = new Account();
        assertThat(account.getId()).isNull();
        assertThat(account.getAccountNumber()).isNull();
    }

    @Test
    void settersUpdateFields() {
        Account account = new Account();
        account.setId(5L);
        account.setAccountNumber("5300000010");
        account.setBalance(new BigDecimal("999.99"));
        account.setStatus(AccountStatus.INACTIVE);

        assertThat(account.getId()).isEqualTo(5L);
        assertThat(account.getAccountNumber()).isEqualTo("5300000010");
        assertThat(account.getBalance()).isEqualByComparingTo("999.99");
        assertThat(account.getStatus()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void accountTypeEnumValues() {
        assertThat(AccountType.values())
                .containsExactlyInAnyOrder(AccountType.SAVINGS, AccountType.CHECKING);
    }

    @Test
    void accountStatusEnumValues() {
        assertThat(AccountStatus.values())
                .containsExactlyInAnyOrder(AccountStatus.ACTIVE, AccountStatus.INACTIVE);
    }

    @Test
    void equalsAndHashCodeWorkCorrectly() {
        Account a1 = Account.builder().id(1L).accountNumber("5300000001")
                .accountType(AccountType.SAVINGS).customerId(1L).build();
        Account a2 = Account.builder().id(1L).accountNumber("5300000001")
                .accountType(AccountType.SAVINGS).customerId(1L).build();

        assertThat(a1).isEqualTo(a2);
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }
}
