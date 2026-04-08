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
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RepositoryRestController
@RequiredArgsConstructor
@Tag(name = "Cuentas", description = "Gestión de cuentas bancarias: POST via controlador, GET y DELETE via Spring Data REST en /api/v1/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/accounts")
    @ResponseBody
    @Operation(summary = "Crear cuenta bancaria",
            description = "Crea una cuenta de ahorro (53XXXXXXXX) o corriente (33XXXXXXXX) para un cliente. Número de cuenta autogenerado y único.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Cuenta creada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Cuenta creada exitosamente", response));
    }

    @GetMapping("/accounts/{id}/balance")
    @ResponseBody
    @Operation(summary = "Consultar saldo de cuenta",
            description = "Retorna el saldo actual de una cuenta bancaria.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Saldo consultado exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountBalance(
            @Parameter(description = "ID de la cuenta") @PathVariable Long id) {
        AccountResponse response = accountService.getAccountBalance(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
