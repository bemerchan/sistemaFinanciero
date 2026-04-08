package com.flypass.financial.controller;

import com.flypass.financial.dto.request.CustomerRequest;
import com.flypass.financial.dto.response.ApiResponse;
import com.flypass.financial.dto.response.CustomerResponse;
import com.flypass.financial.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RepositoryRestController
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes: POST y PUT via controlador, GET y DELETE via Spring Data REST en /api/v1/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/customers")
    @ResponseBody
    @Operation(summary = "Crear un nuevo cliente",
            description = "Crea un nuevo cliente. Valida mayoría de edad, unicidad de correo e identificación.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Correo o identificación ya existe"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Cliente menor de edad")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cliente creado exitosamente", response));
    }

    @PutMapping("/customers/{id}")
    @ResponseBody
    @Operation(summary = "Actualizar cliente",
            description = "Actualiza los datos de un cliente existente. Valida unicidad de correo e identificación.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Correo o identificación ya existe"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Cliente menor de edad")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @Parameter(description = "ID del cliente") @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Cliente actualizado exitosamente", response));
    }
}
