package com.payment.paymentmobileclient.dto;

import java.time.Instant;

public record SubscriptionDto(
        Long id,
        Long serviceId,
        String methodName,
        Instant validFrom,
        Instant validTo,
        String token
) {}