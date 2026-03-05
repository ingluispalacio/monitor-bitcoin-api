package com.micro.service.crypto_monitor.exceptions;

import com.micro.service.crypto_monitor.dto.ErrorResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.security.access.AccessDeniedException;

import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleRuntime(RuntimeException ex) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .success(false)
                .message(ex.getMessage() != null ? ex.getMessage() : "Error de ejecución")
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleGeneric(Exception ex) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .success(false)
                .message("Error interno del servidor: " + ex.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response));
    }

    // Manejo específico para errores de validación
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleValidation(WebExchangeBindException ex) {
        String errorMessage = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Error de validación");

        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .success(false)
                .message(errorMessage)
                .status(HttpStatus.BAD_REQUEST.value())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response));
    }

    // Manejo específico para acceso denegado
    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleAccessDenied(AccessDeniedException ex) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .success(false)
                .message("Acceso denegado: " + ex.getMessage())
                .status(HttpStatus.FORBIDDEN.value())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response));
    }

    // Manejo para NullPointerException
    @ExceptionHandler(NullPointerException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleNullPointer(NullPointerException ex) {
        ErrorResponseDTO response = ErrorResponseDTO.builder()
                .success(false)
                .message("Error: referencia nula no esperada")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response));
    }
}