package com.flypass.financial.exception;

import com.flypass.financial.dto.response.ErrorResponse;
import com.flypass.financial.dto.response.ErrorResponse.FieldErrorDetail;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(org.springframework.data.rest.webmvc.ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSdrResourceNotFound(
            org.springframework.data.rest.webmvc.ResourceNotFoundException ex) {
        log.warn("Recurso SDR no encontrado: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "El recurso solicitado no fue encontrado", null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Regla de negocio violada: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        log.warn("Conflicto de datos: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(UnderageCustomerException.class)
    public ResponseEntity<ErrorResponse> handleUnderage(UnderageCustomerException ex) {
        log.warn("Cliente menor de edad: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        log.warn("Fondos insuficientes: {}", ex.getMessage());
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> FieldErrorDetail.builder()
                        .field(fe.getField())
                        .rejectedValue(fe.getRejectedValue())
                        .message(fe.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ex.getBindingResult().getGlobalErrors().forEach(ge ->
                errors.add(FieldErrorDetail.builder()
                        .field(ge.getObjectName())
                        .message(ge.getDefaultMessage())
                        .build()));

        log.warn("Errores de validación ({}): {}", errors.size(), errors);
        return build(HttpStatus.BAD_REQUEST, "Error de validación en los campos ingresados", errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldErrorDetail> errors = ex.getConstraintViolations().stream()
                .map(cv -> FieldErrorDetail.builder()
                        .field(extractField(cv))
                        .rejectedValue(cv.getInvalidValue() != null ? cv.getInvalidValue().toString() : null)
                        .message(cv.getMessage())
                        .build())
                .collect(Collectors.toList());

        log.warn("Violación de restricciones ({}): {}", errors.size(), errors);
        return build(HttpStatus.BAD_REQUEST, "Error de validación de restricciones", errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMessage());
        String message = "Violación de integridad de datos: el registro ya existe o tiene dependencias";
        if (ex.getMessage() != null && ex.getMessage().contains("unique")) {
            message = "Ya existe un registro con los mismos datos únicos";
        }
        return build(HttpStatus.CONFLICT, message, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Cuerpo de la solicitud inválido: {}", ex.getMessage());
        String detail = "El cuerpo de la solicitud es inválido o está mal formado";
        if (ex.getMessage() != null && ex.getMessage().contains("Cannot deserialize value of type")) {
            detail = "Valor de campo inválido: verifique los tipos y formatos de los campos";
        }
        return build(HttpStatus.BAD_REQUEST, detail, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        List<FieldErrorDetail> errors = List.of(
                FieldErrorDetail.builder()
                        .field(ex.getName())
                        .rejectedValue(ex.getValue() != null ? ex.getValue().toString() : null)
                        .message(String.format("Se esperaba un valor de tipo '%s'",
                                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido"))
                        .build());
        log.warn("Tipo de parámetro incorrecto: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Tipo de parámetro incorrecto", errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        List<FieldErrorDetail> errors = List.of(
                FieldErrorDetail.builder()
                        .field(ex.getParameterName())
                        .message(String.format("El parámetro '%s' es requerido", ex.getParameterName()))
                        .build());
        log.warn("Parámetro requerido faltante: {}", ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, "Parámetro requerido faltante", errors);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ErrorResponse> handleMissingPathVariable(MissingPathVariableException ex) {
        List<FieldErrorDetail> errors = List.of(
                FieldErrorDetail.builder()
                        .field(ex.getVariableName())
                        .message(String.format("La variable de ruta '%s' es requerida", ex.getVariableName()))
                        .build());
        log.warn("Variable de ruta faltante: {}", ex.getVariableName());
        return build(HttpStatus.BAD_REQUEST, "Variable de ruta requerida faltante", errors);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String message = String.format("El método HTTP '%s' no está soportado para esta ruta. Métodos permitidos: %s",
                ex.getMethod(), ex.getSupportedHttpMethods());
        log.warn("Método HTTP no soportado: {}", ex.getMessage());
        return build(HttpStatus.METHOD_NOT_ALLOWED, message, null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        String message = String.format("El tipo de contenido '%s' no está soportado. Use: application/json",
                ex.getContentType());
        log.warn("Tipo de medio no soportado: {}", ex.getMessage());
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, null);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex) {
        String message = String.format("No se encontró el recurso: %s %s", ex.getHttpMethod(), ex.getRequestURL());
        log.warn("Ruta no encontrada: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, message, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento ilegal: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Error inesperado [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno en el servidor. Por favor intente de nuevo más tarde.", null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, List<FieldErrorDetail> errors) {
        return ResponseEntity.status(status).body(
                ErrorResponse.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .errors(errors)
                        .build());
    }

    private String extractField(ConstraintViolation<?> cv) {
        String path = cv.getPropertyPath().toString();
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }
}
