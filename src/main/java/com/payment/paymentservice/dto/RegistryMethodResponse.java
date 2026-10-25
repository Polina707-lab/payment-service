package com.payment.paymentservice.dto;

public record RegistryMethodResponse(
        String serviceName, //название зарегистрированного сервиса
        String methodName, //название его метода
        String serviceUrl //адрес, по которому работает прикладной сервис
) {
}