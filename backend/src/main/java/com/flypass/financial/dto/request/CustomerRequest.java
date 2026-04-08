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

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+$",
            message = "El nombre solo puede contener letras y espacios")
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+$",
            message = "El apellido solo puede contener letras y espacios")
    private String lastName;

    @NotNull(message = "El tipo de identificación es obligatorio. Valores válidos: CC, CE, TI, PASSPORT, NIT")
    private IdentificationType identificationType;

    @NotBlank(message = "El número de identificación es obligatorio")
    @Size(min = 5, max = 50, message = "El número de identificación debe tener entre 5 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9\\-]+$",
            message = "El número de identificación solo puede contener letras, números y guiones")
    private String identificationNumber;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico no es válido (ej: usuario@dominio.com)")
    @Size(max = 150, message = "El correo electrónico no puede exceder 150 caracteres")
    private String email;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
