package com.payment.paymentservice.service;

import com.payment.paymentservice.client.ApplicationServiceClient;
import com.payment.paymentservice.client.RegistryClient;
import com.payment.paymentservice.dto.PaymentRequest;
import com.payment.paymentservice.dto.PaymentResponse;
import com.payment.paymentservice.dto.RegistryMethodResponse;
import com.payment.paymentservice.dto.SubscriptionRequest;
import com.payment.paymentservice.entity.Subscription;
import com.payment.paymentservice.repository.SubscriptionRepository;

import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

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

    public PaymentResponse processPayment(PaymentRequest request) {

        // 1. Проверяем период подписки
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

        // 2. Проверяем метод через реестр
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

        // 3. Генерируем токен
        String token = UUID.randomUUID().toString();

        // 4. Формируем данные для прикладного сервиса
        SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
                request.companyName(),
                request.methodName(),
                request.validFrom(),
                request.validTo(),
                token
        );

        // 5. Передаём токен прикладному сервису
        applicationServiceClient.registerSubscription(
                method.serviceUrl(),
                subscriptionRequest
        );

        // 6. Создаём и сохраняем подписку
        Subscription subscription = new Subscription(
                request.companyName(),
                request.serviceName(),
                request.methodName(),
                request.validFrom(),
                request.validTo(),
                token
        );

        Subscription savedSubscription =
                subscriptionRepository.saveAndFlush(subscription);

        // 7. Возвращаем результат
        return new PaymentResponse(
                savedSubscription.getId(),
                savedSubscription.getToken(),
                "SUCCESS"
        );
    }
}
