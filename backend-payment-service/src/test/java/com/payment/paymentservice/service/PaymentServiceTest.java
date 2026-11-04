
package com.payment.paymentservice.service;

import com.payment.paymentservice.client.ApplicationServiceClient;
import com.payment.paymentservice.client.RegistryClient;
import com.payment.paymentservice.dto.PaymentRequest;
import com.payment.paymentservice.dto.PaymentResponse;
import com.payment.paymentservice.dto.RegistryMethodResponse;
import com.payment.paymentservice.dto.SubscriptionRequest;
import com.payment.paymentservice.entity.Subscription;
import com.payment.paymentservice.exception.MethodNotFoundException;
import com.payment.paymentservice.repository.SubscriptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private SubscriptionRepository repository;
    private RegistryClient registryClient;
    private ApplicationServiceClient applicationServiceClient;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        repository = mock(SubscriptionRepository.class);
        registryClient = mock(RegistryClient.class);
        applicationServiceClient = mock(ApplicationServiceClient.class);

        paymentService = new PaymentService(
                repository,
                registryClient,
                applicationServiceClient
        );
    }

    @Test
    void shouldAcceptValidPeriodAndRegisteredMethod() {

        PaymentRequest request = new PaymentRequest(
                1L,
                "GetProducts",
                Instant.parse("2026-11-01T00:00:00Z"),
                Instant.parse("2026-11-30T23:59:59Z")
        );

        when(registryClient.findMethod(
                1L, "GetProducts"
        )).thenReturn(Optional.of(
                new RegistryMethodResponse(
                        1L,
                        "GetProducts",
                        "http://localhost:8083"
                )
        ));

        when(repository.saveAndFlush(any(Subscription.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processPayment(request);

        assertEquals("SUCCESS", response.status());
        assertNotNull(response.token());

        verify(applicationServiceClient).registerSubscription(
                eq("http://localhost:8083"),
                argThat(subscriptionRequest ->
                        subscriptionRequest.token().equals(response.token())
                                && subscriptionRequest.methodName().equals("GetProducts")
                )
        );

        verify(repository).saveAndFlush(
                argThat(subscription ->
                        subscription.getToken().equals(response.token())
                                && subscription.getServiceId().equals(1L)
                                && subscription.getMethodName().equals("GetProducts")
                                && subscription.getValidFrom().equals(request.validFrom())
                                && subscription.getValidTo().equals(request.validTo())
                )
        );
    }

    @Test
    void shouldRejectInvalidPeriod() {

        PaymentRequest request = new PaymentRequest(
                1L,
                "GetProducts",
                Instant.parse("2026-11-30T00:00:00Z"),
                Instant.parse("2026-11-01T00:00:00Z")
        );

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(request)
        );

        verifyNoInteractions(registryClient);
        verifyNoInteractions(applicationServiceClient);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectUnknownMethod() {

        PaymentRequest request = new PaymentRequest(
                1L,
                "UnknownMethod",
                Instant.parse("2026-11-01T00:00:00Z"),
                Instant.parse("2026-11-30T23:59:59Z")
        );

        when(registryClient.findMethod(
                1L, "UnknownMethod"
        )).thenReturn(Optional.empty());

        assertThrows(MethodNotFoundException.class, () ->
                paymentService.processPayment(request)
        );

        verifyNoInteractions(applicationServiceClient);
        verifyNoInteractions(repository);
    }

    @Test
    void shouldNotSaveSubscriptionWhenApplicationServiceFails() {

        PaymentRequest request = new PaymentRequest(
                1L,
                "GetProducts",
                Instant.parse("2026-11-01T00:00:00Z"),
                Instant.parse("2026-11-30T23:59:59Z")
        );

        when(registryClient.findMethod(
                1L, "GetProducts"
        )).thenReturn(Optional.of(
                new RegistryMethodResponse(
                        1L,
                        "GetProducts",
                        "http://localhost:8083"
                )
        ));

        doThrow(new IllegalStateException("Сервис недоступен"))
                .when(applicationServiceClient)
                .registerSubscription(
                        eq("http://localhost:8083"),
                        any(SubscriptionRequest.class)
                );

        assertThrows(IllegalStateException.class, () ->
                paymentService.processPayment(request)
        );

        verify(repository, never())
                .saveAndFlush(any(Subscription.class));
    }
}
