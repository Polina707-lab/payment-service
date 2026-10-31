package com.payment.paymentmobileclient.dto;

public record PaymentResponseDto(
        Long subscriptionId,
        String token,
        String status
) {}