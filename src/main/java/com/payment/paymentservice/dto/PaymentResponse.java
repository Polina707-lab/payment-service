package com.payment.paymentservice.dto;
//Данные, которые Payment Service возвращает клиенту
public record PaymentResponse(
        Long subscriptionId,
        String token,
        String status
) {
}