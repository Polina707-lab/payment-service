package com.payment.paymentservice.service;

import com.payment.paymentservice.dto.ValidationRequest;
import com.payment.paymentservice.dto.ValidationResponse;
import com.payment.paymentservice.entity.Subscription;
import com.payment.paymentservice.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class ValidationService {

    private final SubscriptionRepository subscriptionRepository;

    public ValidationService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public ValidationResponse validate(ValidationRequest request) {

        Optional<Subscription> optionalSubscription =
                subscriptionRepository.findByToken(request.token());

        // Токен не найден
        if (optionalSubscription.isEmpty()) {
            return new ValidationResponse("BAD");
        }

        Subscription subscription = optionalSubscription.get();

        // Токен принадлежит другому сервису или методу
        if (!subscription.getServiceId().equals(request.serviceId())
                || !subscription.getMethodName().equals(request.methodName())) {
            return new ValidationResponse("BAD");
        }

        Instant now = Instant.now();

        // Срок действия подписки закончился
        if (now.isAfter(subscription.getValidTo())) {
            return new ValidationResponse("EXPIRED");
        }

        // Период действия подписки ещё не начался
        if (now.isBefore(subscription.getValidFrom())) {
            return new ValidationResponse("BAD");
        }

        // Подписка действительна
        return new ValidationResponse("OK");
    }
}