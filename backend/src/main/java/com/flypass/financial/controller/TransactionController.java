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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transacciones", description = "Registro y consulta de transacciones bancarias")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/account/{accountId}")
    @Operation(summary = "Registrar transacción",
            description = "Registra una consignación (DEPOSIT) o retiro (WITHDRAWAL) en una cuenta. "
                    + "Los retiros requieren saldo suficiente. Las cuentas de ahorro no pueden quedar con saldo negativo.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "Transacción registrada exitosamente"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Datos de entrada inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Cuenta no encontrada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422",
                    description = "Fondos insuficientes o cuenta inactiva")
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> registerTransaction(
            @Parameter(description = "ID de la cuenta") @PathVariable Long accountId,
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.registerTransaction(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Transacción registrada exitosamente", response));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Listar movimientos de una cuenta",
            description = "Retorna todos los movimientos de una cuenta, ordenados de más reciente a más antiguo.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Lista de transacciones"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Cuenta no encontrada")
    })
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByAccount(
            @Parameter(description = "ID de la cuenta") @PathVariable Long accountId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByAccount(accountId);
        return ResponseEntity.ok(ApiResponse.ok(transactions));
    }

    @GetMapping("/account/{accountId}/last")
    @Operation(summary = "Últimos movimientos de una cuenta",
            description = "Retorna los últimos N movimientos de una cuenta. Por defecto retorna los últimos 5.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Últimas transacciones"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Cuenta no encontrada")
    })
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getLastTransactions(
            @Parameter(description = "ID de la cuenta") @PathVariable Long accountId,
            @Parameter(description = "Número de transacciones a retornar (default: 5)")
            @RequestParam(defaultValue = "5") int limit) {
        List<TransactionResponse> transactions =
                transactionService.getLastTransactionsByAccount(accountId, limit);
        return ResponseEntity.ok(ApiResponse.ok(transactions));
    }
}
