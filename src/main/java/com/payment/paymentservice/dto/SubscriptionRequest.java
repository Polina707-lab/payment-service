
package com.payment.paymentservice.dto;

// Данные, отправляемые в Application Service после оплаты
public record SubscriptionRequest(
        String methodName,
        String token
) {
}
