
package com.payment.paymentservice.dto;

// Данные, полученные от Registry Service
public record RegistryMethodResponse(
        Long serviceId,
        String methodName,
        String serviceUrl   // адрес прикладного сервиса
) {
}
