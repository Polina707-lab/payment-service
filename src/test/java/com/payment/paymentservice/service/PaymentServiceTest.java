package com.payment.paymentservice.service;

import com.payment.paymentservice.client.RegistryClient;
import com.payment.paymentservice.dto.PaymentRequest;
import com.payment.paymentservice.repository.SubscriptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    private RegistryClient registryClient;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        SubscriptionRepository repository =
                mock(SubscriptionRepository.class);

        registryClient = mock(RegistryClient.class);

        paymentService = new PaymentService(
                repository,
                registryClient
        );
    }

    @Test
    void shouldAcceptValidPeriodAndRegisteredMethod() {
        PaymentRequest request = new PaymentRequest(
                "CompanyA",
                "ProductService",
                "GetProducts",
                Instant.parse("2026-11-01T00:00:00Z"),
                Instant.parse("2026-11-30T23:59:59Z")
        );

        when(registryClient.methodExists(
                "ProductService", "GetProducts"
        )).thenReturn(true);

        assertDoesNotThrow(() ->
                paymentService.processPayment(request)
        );
    }

    @Test
    void shouldRejectInvalidPeriod() {
        PaymentRequest request = new PaymentRequest(
                "CompanyA",
                "ProductService",
                "GetProducts",
                Instant.parse("2026-11-30T00:00:00Z"),
                Instant.parse("2026-11-01T00:00:00Z")
        );

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(request)
        );

        verifyNoInteractions(registryClient);
    }

    @Test
    void shouldRejectUnknownMethod() {
        PaymentRequest request = new PaymentRequest(
                "CompanyA",
                "ProductService",
                "UnknownMethod",
                Instant.parse("2026-11-01T00:00:00Z"),
                Instant.parse("2026-11-30T23:59:59Z")
        );

        when(registryClient.methodExists(
                "ProductService", "UnknownMethod"
        )).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                paymentService.processPayment(request)
        );
    }
}