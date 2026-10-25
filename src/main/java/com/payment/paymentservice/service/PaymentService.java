package com.payment.paymentservice.service;

import com.payment.paymentservice.client.RegistryClient;
import com.payment.paymentservice.dto.PaymentRequest;
import com.payment.paymentservice.repository.SubscriptionRepository;
import com.payment.paymentservice.client.ApplicationServiceClient;
import org.springframework.stereotype.Service;
import com.payment.paymentservice.dto.RegistryMethodResponse;
import java.util.Objects;


@Service
public class PaymentService {

    private final SubscriptionRepository subscriptionRepository;
    private final RegistryClient registryClient;
    private final ApplicationServiceClient applicationServiceClient;

    public PaymentService(
            SubscriptionRepository subscriptionRepository,
            RegistryClient registryClient,
            ApplicationServiceClient applicationServiceClient
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.registryClient = registryClient;
        this.applicationServiceClient = applicationServiceClient;
    }

    public void processPayment(PaymentRequest request) {

        if (request.validFrom() == null || request.validTo() == null) {
            throw new IllegalArgumentException(
                    "Необходимо указать начало и окончание периода"
            );
        }

        if (!request.validTo().isAfter(request.validFrom())) {
            throw new IllegalArgumentException(
                    "Дата окончания должна быть позже даты начала"
            );
        }

        RegistryMethodResponse method = registryClient.findMethod(
                request.serviceName(),
                request.methodName()
        ).orElseThrow(() -> new IllegalArgumentException(
                "Указанный сервис или метод не зарегистрирован"
        ));

        if (!Objects.equals(method.serviceName(), request.serviceName())
                || !Objects.equals(method.methodName(), request.methodName())
                || method.serviceUrl() == null
                || method.serviceUrl().isBlank()) {
            throw new IllegalStateException(
                    "Реестр вернул некорректную информацию о методе"
            );
        }

    }
}