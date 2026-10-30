
package com.payment.paymentservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.payment.paymentservice.entity.Subscription;
import com.payment.paymentservice.repository.SubscriptionRepository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PaymentIntegrationTest {

    private static final WireMockServer registryServer =
            new WireMockServer(0);

    private static final WireMockServer applicationServer =
            new WireMockServer(0);

    private static final String REGISTRY_PATH =
            "/api/services?serviceId=1&methodName=GetProducts";

    static {
        registryServer.start();
        applicationServer.start();
    }

    @LocalServerPort
    private int port;

    @Autowired
    private SubscriptionRepository repository;

    private final RestClient httpClient = RestClient.create();

    private final JsonMapper jsonMapper = new JsonMapper();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("registry.base-url", registryServer::baseUrl);
    }

    @BeforeEach
    void resetServers() {
        registryServer.resetAll();
        applicationServer.resetAll();
    }

    @AfterAll
    static void stopServers() {
        registryServer.stop();
        applicationServer.stop();
    }

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {

        String registryResponse = """
                {
                  "serviceId": 1,
                  "methodName": "GetProducts",
                  "serviceUrl": "%s"
                }
                """.formatted(applicationServer.baseUrl());

        registryServer.stubFor(
                get(urlEqualTo(REGISTRY_PATH))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody(registryResponse)
                        )
        );

        applicationServer.stubFor(
                post(urlEqualTo("/api/subscriptions"))
                        .willReturn(
                                aResponse().withStatus(201)
                        )
        );

        String requestJson = """
                {
                  "serviceId": 1,
                  "methodName": "GetProducts",
                  "validFrom": "2026-11-01T00:00:00Z",
                  "validTo": "2026-11-30T23:59:59Z"
                }
                """;

        Long savedId = null;

        try {
            PaymentResult response = httpClient.post()
                    .uri("http://localhost:" + port + "/api/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestJson)
                    .retrieve()
                    .body(PaymentResult.class);

            assertNotNull(response);
            savedId = response.subscriptionId();

            assertEquals("SUCCESS", response.status());
            assertNotNull(savedId);
            assertNotNull(response.token());

            // Проверяем запись в MySQL
            Subscription saved = repository
                    .findById(savedId)
                    .orElseThrow();

            assertEquals(1L, saved.getServiceId());
            assertEquals("GetProducts", saved.getMethodName());
            assertEquals(
                    Instant.parse("2026-11-01T00:00:00Z"),
                    saved.getValidFrom()
            );
            assertEquals(
                    Instant.parse("2026-11-30T23:59:59Z"),
                    saved.getValidTo()
            );
            assertEquals(response.token(), saved.getToken());

            // Проверяем обращение к Registry Service
            registryServer.verify(
                    1,
                    getRequestedFor(urlEqualTo(REGISTRY_PATH))
            );

            // Проверяем обращение к Application Service
            applicationServer.verify(
                    1,
                    postRequestedFor(urlEqualTo("/api/subscriptions"))
            );

            // Получаем фактически отправленный HTTP-запрос
            List<LoggedRequest> requests =
                    applicationServer.findAll(
                            postRequestedFor(
                                    urlEqualTo("/api/subscriptions")
                            )
                    );

            assertEquals(1, requests.size());

            LoggedRequest sentRequest = requests.get(0);
            String sentJson = sentRequest.getBodyAsString();

            assertFalse(
                    sentJson.isBlank(),
                    "Application Service получил пустое тело HTTP-запроса"
            );

            JsonNode body = jsonMapper.readTree(sentJson);

            // Проверяем новый контракт: только два поля
            assertEquals(2, body.size());

            assertEquals(
                    "GetProducts",
                    body.get("methodName").asText()
            );

            assertEquals(
                    response.token(),
                    body.get("token").asText()
            );

        } finally {
            // Удаляем тестовую запись даже при ошибке проверки
            if (savedId != null && repository.existsById(savedId)) {
                repository.deleteById(savedId);
            }
        }
    }

    @Test
    void shouldReturnBadGatewayWhenRegistryFails() {

        // Registry Service возвращает HTTP 503
        registryServer.stubFor(
                get(urlEqualTo(REGISTRY_PATH))
                        .willReturn(
                                aResponse().withStatus(503)
                        )
        );

        String requestJson = """
                {
                  "serviceId": 1,
                  "methodName": "GetProducts",
                  "validFrom": "2026-11-01T00:00:00Z",
                  "validTo": "2026-11-30T23:59:59Z"
                }
                """;

        long countBefore = repository.count();

        var response = httpClient.post()
                .uri("http://localhost:" + port + "/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        (request, clientResponse) -> {
                            // Проверяем HTTP-статус самостоятельно
                        }
                )
                .toBodilessEntity();

        assertEquals(502, response.getStatusCode().value());

        registryServer.verify(
                1,
                getRequestedFor(urlEqualTo(REGISTRY_PATH))
        );

        applicationServer.verify(
                0,
                postRequestedFor(urlEqualTo("/api/subscriptions"))
        );

        assertEquals(countBefore, repository.count());
    }

    @Test
    void shouldReturnBadGatewayWhenApplicationServiceFails() {

        String registryResponse = """
                {
                  "serviceId": 1,
                  "methodName": "GetProducts",
                  "serviceUrl": "%s"
                }
                """.formatted(applicationServer.baseUrl());

        registryServer.stubFor(
                get(urlEqualTo(REGISTRY_PATH))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody(registryResponse)
                        )
        );

        // Application Service возвращает ошибку
        applicationServer.stubFor(
                post(urlEqualTo("/api/subscriptions"))
                        .willReturn(
                                aResponse().withStatus(500)
                        )
        );

        String requestJson = """
                {
                  "serviceId": 1,
                  "methodName": "GetProducts",
                  "validFrom": "2026-11-01T00:00:00Z",
                  "validTo": "2026-11-30T23:59:59Z"
                }
                """;

        long countBefore = repository.count();

        var response = httpClient.post()
                .uri("http://localhost:" + port + "/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        (request, clientResponse) -> {
                        }
                )
                .toBodilessEntity();

        assertEquals(502, response.getStatusCode().value());

        registryServer.verify(
                1,
                getRequestedFor(urlEqualTo(REGISTRY_PATH))
        );

        applicationServer.verify(
                1,
                postRequestedFor(urlEqualTo("/api/subscriptions"))
        );

        assertEquals(countBefore, repository.count());
    }

    @Test
    void shouldRejectInvalidSubscriptionPeriod() {

        String requestJson = """
                {
                  "serviceId": 1,
                  "methodName": "GetProducts",
                  "validFrom": "2026-12-01T00:00:00Z",
                  "validTo": "2026-11-01T00:00:00Z"
                }
                """;

        long countBefore = repository.count();

        var response = httpClient.post()
                .uri("http://localhost:" + port + "/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        (request, clientResponse) -> {
                        }
                )
                .toBodilessEntity();

        assertEquals(400, response.getStatusCode().value());

        registryServer.verify(0, anyRequestedFor(anyUrl()));
        applicationServer.verify(0, anyRequestedFor(anyUrl()));

        assertEquals(countBefore, repository.count());
    }

    @Test
    void shouldRejectInvalidServiceId() {

        String requestJson = """
                {
                  "serviceId": 0,
                  "methodName": "GetProducts",
                  "validFrom": "2026-11-01T00:00:00Z",
                  "validTo": "2026-11-30T23:59:59Z"
                }
                """;

        long countBefore = repository.count();

        var response = httpClient.post()
                .uri("http://localhost:" + port + "/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        (request, clientResponse) -> {
                        }
                )
                .toBodilessEntity();

        assertEquals(400, response.getStatusCode().value());

        registryServer.verify(0, anyRequestedFor(anyUrl()));
        applicationServer.verify(0, anyRequestedFor(anyUrl()));

        assertEquals(countBefore, repository.count());
    }

    record PaymentResult(
            Long subscriptionId,
            String token,
            String status
    ) {
    }
}
