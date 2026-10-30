
package com.payment.paymentservice.repository;

import com.payment.paymentservice.entity.Subscription;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository repository;

    @Test
    void shouldSaveSubscription() {

        String token = UUID.randomUUID().toString();

        Instant validFrom = Instant.parse("2026-11-01T00:00:00Z");
        Instant validTo = Instant.parse("2026-11-30T23:59:59Z");

        Subscription subscription = new Subscription(
                1L,
                "GetProducts",
                validFrom,
                validTo,
                token
        );

        Subscription saved = repository.saveAndFlush(subscription);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getServiceId());
        assertEquals("GetProducts", saved.getMethodName());
        assertEquals(validFrom, saved.getValidFrom());
        assertEquals(validTo, saved.getValidTo());
        assertEquals(token, saved.getToken());

        assertTrue(repository.findById(saved.getId()).isPresent());
    }
}
