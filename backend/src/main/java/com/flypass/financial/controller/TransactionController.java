package com.flypass.financial.controller;

import com.flypass.financial.dto.request.TransactionRequest;
import com.flypass.financial.dto.response.ApiResponse;
import com.flypass.financial.dto.response.TransactionResponse;
import com.flypass.financial.service.TransactionService;
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
@Tag(name = "Transacciones - Escritura", description = "Registro de transacciones bancarias. Consultas via Spring Data REST en /api/v1/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transactions/account/{accountId}")
    @ResponseBody
    @Operation(summary = "Registrar transacción",
            description = "Registra una consignación (DEPOSIT) o retiro (WITHDRAWAL) en una cuenta. "
                    + "Los retiros requieren saldo suficiente. Las cuentas de ahorro no pueden quedar con saldo negativo.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Transacción registrada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Fondos insuficientes o cuenta inactiva")
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> registerTransaction(
            @Parameter(description = "ID de la cuenta") @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.registerTransaction(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transacción registrada exitosamente", response));
    }
}
