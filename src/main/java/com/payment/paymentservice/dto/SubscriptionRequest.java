package com.payment.paymentservice.dto;

import java.time.Instant;

public record SubscriptionRequest(
        String companyName,
        String methodName,
        Instant validFrom,
        Instant validTo,
        String token
) {
}