package com.payment.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

// Данные для оформления оплаченной подписки
public record PaymentRequest(
        @NotNull @Positive Long serviceId,
        @NotBlank String methodName,
        @NotNull Instant validFrom,
        @NotNull Instant validTo
) {
}