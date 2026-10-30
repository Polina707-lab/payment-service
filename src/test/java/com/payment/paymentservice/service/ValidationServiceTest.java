package com.payment.paymentservice.service;

import com.payment.paymentservice.dto.ValidationRequest;
import com.payment.paymentservice.dto.ValidationResponse;
import com.payment.paymentservice.entity.Subscription;
import com.payment.paymentservice.repository.SubscriptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ValidationServiceTest {

    private SubscriptionRepository subscriptionRepository;
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        subscriptionRepository = mock(SubscriptionRepository.class);
        validationService = new ValidationService(subscriptionRepository);
    }

    @Test
    void shouldReturnOkForValidSubscription() {

        Instant now = Instant.now();

        Subscription subscription = new Subscription(
                1L,
                "GetProducts",
                now.minusSeconds(3600),
                now.plusSeconds(3600),
                "test-token"
        );

        when(subscriptionRepository.findByToken("test-token"))
                .thenReturn(Optional.of(subscription));

        ValidationRequest request = new ValidationRequest(
                1L,
                "GetProducts",
                "test-token"
        );

        ValidationResponse response = validationService.validate(request);

        assertEquals("OK", response.verdict());
    }

    @Test
    void shouldReturnBadForUnknownToken() {

        when(subscriptionRepository.findByToken("unknown-token"))
                .thenReturn(Optional.empty());

        ValidationRequest request = new ValidationRequest(
                1L,
                "GetProducts",
                "unknown-token"
        );

        ValidationResponse response = validationService.validate(request);

        assertEquals("BAD", response.verdict());
    }

    @Test
    void shouldReturnBadForWrongServiceId() {

        Instant now = Instant.now();

        Subscription subscription = new Subscription(
                1L,
                "GetProducts",
                now.minusSeconds(3600),
                now.plusSeconds(3600),
                "test-token"
        );

        when(subscriptionRepository.findByToken("test-token"))
                .thenReturn(Optional.of(subscription));

        ValidationRequest request = new ValidationRequest(
                2L,
                "GetProducts",
                "test-token"
        );

        ValidationResponse response = validationService.validate(request);

        assertEquals("BAD", response.verdict());
    }

    @Test
    void shouldReturnBadForWrongMethodName() {

        Instant now = Instant.now();

        Subscription subscription = new Subscription(
                1L,
                "GetProducts",
                now.minusSeconds(3600),
                now.plusSeconds(3600),
                "test-token"
        );

        when(subscriptionRepository.findByToken("test-token"))
                .thenReturn(Optional.of(subscription));

        ValidationRequest request = new ValidationRequest(
                1L,
                "DeleteProducts",
                "test-token"
        );

        ValidationResponse response = validationService.validate(request);

        assertEquals("BAD", response.verdict());
    }

    @Test
    void shouldReturnExpiredForExpiredSubscription() {

        Instant now = Instant.now();

        Subscription subscription = new Subscription(
                1L,
                "GetProducts",
                now.minusSeconds(7200),
                now.minusSeconds(3600),
                "test-token"
        );

        when(subscriptionRepository.findByToken("test-token"))
                .thenReturn(Optional.of(subscription));

        ValidationRequest request = new ValidationRequest(
                1L,
                "GetProducts",
                "test-token"
        );

        ValidationResponse response = validationService.validate(request);

        assertEquals("EXPIRED", response.verdict());
    }

    @Test
    void shouldReturnBadForSubscriptionNotYetActive() {

        Instant now = Instant.now();

        Subscription subscription = new Subscription(
                1L,
                "GetProducts",
                now.plusSeconds(3600),
                now.plusSeconds(7200),
                "test-token"
        );

        when(subscriptionRepository.findByToken("test-token"))
                .thenReturn(Optional.of(subscription));

        ValidationRequest request = new ValidationRequest(
                1L,
                "GetProducts",
                "test-token"
        );

        ValidationResponse response = validationService.validate(request);

        assertEquals("BAD", response.verdict());
    }
}