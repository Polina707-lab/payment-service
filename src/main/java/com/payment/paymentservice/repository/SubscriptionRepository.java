package com.payment.paymentservice.repository;

import com.payment.paymentservice.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Subscription — тип сущности, с которой мы работаем
// Long — тип её первичного ключа id
public interface SubscriptionRepository
        extends JpaRepository<Subscription, Long> {

    // Поиск подписки по токену
    Optional<Subscription> findByToken(String token);
}