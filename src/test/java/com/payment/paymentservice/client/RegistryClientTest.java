package com.payment.paymentservice.client;

import com.payment.paymentservice.dto.RegistryMethodResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class RegistryClientTest {

    private RegistryClient registryClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {

        RestClient.Builder builder = RestClient.builder();

        mockServer = MockRestServiceServer
                .bindTo(builder)
                .build();

        registryClient = new RegistryClient(
                builder,
                "http://localhost:8082"
        );
    }

    @Test
    void shouldReturnMethodWhenRegistered() {

        String json = """
                {
                  "serviceName": "ProductService",
                  "methodName": "GetProducts",
                  "serviceUrl": "http://localhost:8083"
                }
                """;

        mockServer.expect(requestTo(
                        "http://localhost:8082/api/services/ProductService/methods/GetProducts"
                ))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess(
                        json,
                        MediaType.APPLICATION_JSON
                ));

        Optional<RegistryMethodResponse> result =
                registryClient.findMethod(
                        "ProductService",
                        "GetProducts"
                );

        assertTrue(result.isPresent());

        RegistryMethodResponse response = result.get();

        assertEquals("ProductService", response.serviceName());
        assertEquals("GetProducts", response.methodName());
        assertEquals("http://localhost:8083", response.serviceUrl());

        mockServer.verify();
    }

    @Test
    void shouldReturnEmptyWhenMethodNotFound() {

        mockServer.expect(requestTo(
                        "http://localhost:8082/api/services/ProductService/methods/UnknownMethod"
                ))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withResourceNotFound());

        Optional<RegistryMethodResponse> result =
                registryClient.findMethod(
                        "ProductService",
                        "UnknownMethod"
                );

        assertTrue(result.isEmpty());

        mockServer.verify();
    }
}