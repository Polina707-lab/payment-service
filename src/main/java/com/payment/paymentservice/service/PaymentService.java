package com.payment.paymentservice.service;

import com.payment.paymentservice.client.RegistryClient;
import com.payment.paymentservice.dto.PaymentRequest;
import com.payment.paymentservice.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;


@Service
public class PaymentService {

    private final SubscriptionRepository subscriptionRepository;
    private final RegistryClient registryClient;

    public PaymentService(
            SubscriptionRepository subscriptionRepository,
            RegistryClient registryClient
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.registryClient = registryClient;
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

        if (!registryClient.methodExists(
                request.serviceName(),
                request.methodName()
        )) {
            throw new IllegalArgumentException(
                    "Указанный сервис или метод не зарегистрирован"
            );
        }
    }
}