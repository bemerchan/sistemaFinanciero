package com.flypass.financial.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flypass.financial.entity.Customer.IdentificationType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    private String lastName;

    @NotNull(message = "El tipo de identificación es requerido")
    private IdentificationType identificationType;

    @NotBlank(message = "El número de identificación es requerido")
    @Size(min = 5, max = 50, message = "El número de identificación debe tener entre 5 y 50 caracteres")
    private String identificationNumber;

    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "El correo electrónico no es válido")
    @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
    private String email;

    @NotNull(message = "La fecha de nacimiento es requerida")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
