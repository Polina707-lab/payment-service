
package com.payment.paymentservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.payment.paymentservice.dto.SubscriptionRequest;
import com.payment.paymentservice.entity.Subscription;
import com.payment.paymentservice.repository.SubscriptionRepository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentIntegrationTest {

    private static final WireMockServer registryServer =
            new WireMockServer(0);

    private static final WireMockServer applicationServer =
            new WireMockServer(0);

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

        String companyName = "IntegrationTest-" + UUID.randomUUID();

        String registryResponse = """
                {
                  "serviceName": "ProductService",
                  "methodName": "GetProducts",
                  "serviceUrl": "%s"
                }
                """.formatted(applicationServer.baseUrl());

        registryServer.stubFor(
                get(urlEqualTo(
                        "/api/services/ProductService/methods/GetProducts"
                ))
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
                  "companyName": "%s",
                  "serviceName": "ProductService",
                  "methodName": "GetProducts",
                  "validFrom": "2026-11-01T00:00:00Z",
                  "validTo": "2026-11-30T23:59:59Z"
                }
                """.formatted(companyName);

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

            assertEquals(companyName, saved.getCompanyName());
            assertEquals("ProductService", saved.getServiceName());
            assertEquals("GetProducts", saved.getMethodName());
            assertEquals(response.token(), saved.getToken());

            // Проверяем обращение к Registry Service
            registryServer.verify(
                    1,
                    getRequestedFor(urlEqualTo(
                            "/api/services/ProductService/methods/GetProducts"
                    ))
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

            // Проверяем содержимое запроса
            assertEquals(
                    companyName,
                    body.get("companyName").asText()
            );

            assertEquals(
                    "GetProducts",
                    body.get("methodName").asText()
            );

            assertEquals(
                    response.token(),
                    body.get("token").asText()
            );

            assertEquals(
                    "2026-11-01T00:00:00Z",
                    body.get("validFrom").asText()
            );

            assertEquals(
                    "2026-11-30T23:59:59Z",
                    body.get("validTo").asText()
            );

        } finally {
            // Удаляем тестовую запись даже при ошибке проверки
            if (savedId != null && repository.existsById(savedId)) {
                repository.deleteById(savedId);
            }
        }
    }

    record PaymentResult(
            Long subscriptionId,
            String token,
            String status
    ) {
    }

    @Test
    void shouldSerializeSubscriptionRequest() {
        SubscriptionRequest request = new SubscriptionRequest(
                "TestCompany",
                "GetProducts",
                Instant.parse("2026-11-01T00:00:00Z"),
                Instant.parse("2026-11-30T23:59:59Z"),
                UUID.randomUUID().toString()
        );

        String json = jsonMapper.writeValueAsString(request);

        System.out.println("Сериализованный JSON: " + json);

        assertTrue(json.contains("\"companyName\":\"TestCompany\""));
        assertTrue(json.contains("\"methodName\":\"GetProducts\""));
    }
}
