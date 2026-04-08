package com.flypass.financial.dto.response;

import com.flypass.financial.entity.Customer.IdentificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private IdentificationType identificationType;
    private String identificationNumber;
    private String email;
    private LocalDate birthDate;
    private int age;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
