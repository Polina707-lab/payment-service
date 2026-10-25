package com.payment.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            IllegalArgumentException ex
    ) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            MethodNotFoundException ex
    ) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "Некорректные данные запроса"
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidJson(
            HttpMessageNotReadableException ex
    ) {
        return buildError(
                HttpStatus.BAD_REQUEST,
                "Некорректный формат JSON"
        );
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiError> handleExternalServiceError(
            RestClientException ex
    ) {
        return buildError(
                HttpStatus.BAD_GATEWAY,
                "Ошибка взаимодействия с внешним сервисом"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedError(
            Exception ex
    ) {
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Внутренняя ошибка сервера"
        );
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message
    ) {
        ApiError error = new ApiError(
                status.value(),
                message,
                Instant.now()
        );

        return ResponseEntity.status(status).body(error);
    }
}