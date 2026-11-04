package com.payment.paymentmobileclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ValidationRequestDto(
        @JsonProperty("serviceID") Long serviceId,
        String methodName,
        String token
) {}