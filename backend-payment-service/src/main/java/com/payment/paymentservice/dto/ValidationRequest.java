package com.payment.paymentservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ValidationRequest(
        @JsonProperty("serviceID")
        @NotNull
        @Positive
        Long serviceId,

        @NotBlank
        String methodName,

        @NotBlank
        String token
) {
}