package com.flypass.financial.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Size(min = 10, max = 10, message = "El número de cuenta debe tener exactamente 10 dígitos")
    @Pattern(regexp = "^[0-9]{10}$", message = "El número de cuenta debe contener exactamente 10 dígitos numéricos")
    @Column(name = "account_number", nullable = false, unique = true, length = 10)
    private String accountNumber;

    @NotNull(message = "El tipo de cuenta es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @NotNull(message = "El estado de la cuenta es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @NotNull(message = "El saldo es obligatorio")
    @DecimalMin(value = "0.00", inclusive = true, message = "El saldo no puede ser negativo")
    @Digits(integer = 13, fraction = 2, message = "El saldo debe tener máximo 13 enteros y 2 decimales")
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @NotNull(message = "El ID del cliente es obligatorio")
    @Positive(message = "El ID del cliente debe ser un número positivo")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AccountType {
        SAVINGS, CHECKING
    }

    public enum AccountStatus {
        ACTIVE, INACTIVE
    }
}
