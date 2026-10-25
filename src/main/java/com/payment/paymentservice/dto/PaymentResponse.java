package com.payment.paymentservice.dto;

public record PaymentResponse(
        Long subscriptionId,
        String token,
        String status
) {
}