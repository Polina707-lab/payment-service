package com.payment.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record PaymentRequest(
        @NotBlank String companyName,
        @NotBlank String serviceName,
        @NotBlank String methodName,
        @NotNull Instant validFrom,
        @NotNull Instant validTo
) {
}