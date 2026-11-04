package com.payment.paymentmobileclient.dto;

import java.time.Instant;

public record PaymentRequestDto(
        Long serviceId,
        String methodName,
        Instant validFrom,
        Instant validTo
) {}