package com.payment.paymentservice.integration;

import com.payment.paymentservice.entity.Subscription;
import com.payment.paymentservice.repository.SubscriptionRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.springframework.http.MediaType;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ValidationIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void shouldReturnOkForValidSubscription() throws Exception {

        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Subscription subscription = new Subscription(
                1L,
                "GetProducts",
                now.minusSeconds(3600),
                now.plusSeconds(3600),
                token
        );

        subscriptionRepository.saveAndFlush(subscription);

        String json = """
                {
                  "serviceID": 1,
                  "methodName": "GetProducts",
                  "token": "%s"
                }
                """.formatted(token);

        HttpResponse<String> response = sendValidationRequest(json);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"verdict\":\"OK\""));
    }

    @Test
    void shouldReturnExpiredForExpiredSubscription() throws Exception {

        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();

        Subscription subscription = new Subscription(
                1L,
                "GetProducts",
                now.minusSeconds(7200),
                now.minusSeconds(3600),
                token
        );

        subscriptionRepository.saveAndFlush(subscription);

        String json = """
                {
                  "serviceID": 1,
                  "methodName": "GetProducts",
                  "token": "%s"
                }
                """.formatted(token);

        HttpResponse<String> response = sendValidationRequest(json);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"verdict\":\"EXPIRED\""));
    }

    @Test
    void shouldReturnBadForUnknownToken() throws Exception {

        String json = """
                {
                  "serviceID": 1,
                  "methodName": "GetProducts",
                  "token": "%s"
                }
                """.formatted(UUID.randomUUID());

        HttpResponse<String> response = sendValidationRequest(json);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"verdict\":\"BAD\""));
    }

    private HttpResponse<String> sendValidationRequest(String json)
            throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/validate"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .method("GET", HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }
}