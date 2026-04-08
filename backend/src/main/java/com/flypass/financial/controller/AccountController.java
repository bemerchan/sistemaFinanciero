package com.flypass.financial.controller;

import com.flypass.financial.dto.request.AccountRequest;
import com.flypass.financial.dto.response.AccountResponse;
import com.flypass.financial.dto.response.ApiResponse;
import com.flypass.financial.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Cuentas", description = "Gestión de cuentas bancarias")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Crear cuenta bancaria",
            description = "Crea una cuenta de ahorro (prefijo 53XXXXXXXX) o corriente (prefijo 33XXXXXXXX) vinculada a un cliente. El número de cuenta es autogenerado y único.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "Cuenta creada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Cliente no encontrado")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cuenta creada exitosamente", response));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Listar cuentas por cliente",
            description = "Retorna todas las cuentas bancarias asociadas a un cliente.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Lista de cuentas del cliente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Cliente no encontrado")
    })
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomer(
            @Parameter(description = "ID del cliente") @PathVariable Long customerId) {
        List<AccountResponse> accounts = accountService.getAccountsByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.ok(accounts));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cuenta por ID",
            description = "Retorna los datos de una cuenta específica dado su ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Cuenta encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Cuenta no encontrada")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @Parameter(description = "ID de la cuenta") @PathVariable Long id) {
        AccountResponse response = accountService.getAccountById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Consultar saldo de cuenta",
            description = "Retorna el saldo actual de una cuenta bancaria específica.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Saldo de la cuenta"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Cuenta no encontrada")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountBalance(
            @Parameter(description = "ID de la cuenta") @PathVariable Long id) {
        AccountResponse response = accountService.getAccountBalance(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
