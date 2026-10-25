package com.payment.paymentservice.repository;

import com.payment.paymentservice.entity.Subscription;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository repository;

    @Test
    void shouldSaveSubscription() {

        String token = UUID.randomUUID().toString();

        Subscription subscription = new Subscription(
                "CompanyA",
                "ProductService",
                "GetProducts",
                Instant.parse("2026-11-01T00:00:00Z"),
                Instant.parse("2026-11-30T23:59:59Z"),
                token
        );

        Subscription saved = repository.saveAndFlush(subscription);

        assertNotNull(saved.getId());
        assertEquals("CompanyA", saved.getCompanyName());
        assertEquals("ProductService", saved.getServiceName());
        assertEquals("GetProducts", saved.getMethodName());
        assertEquals(token, saved.getToken());

        assertTrue(repository.findById(saved.getId()).isPresent());
    }
}