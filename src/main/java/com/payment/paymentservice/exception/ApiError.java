package com.payment.paymentservice.exception;

import java.time.Instant;
//DTO для ответа об ошибке
public record ApiError(
        int status,
        String message,
        Instant timestamp
) {
}