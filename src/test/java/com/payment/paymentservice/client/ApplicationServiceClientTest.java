
package com.payment.paymentservice.client;

import com.payment.paymentservice.dto.SubscriptionRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpServerErrorException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ApplicationServiceClientTest {

    private ApplicationServiceClient applicationServiceClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();

        mockServer = MockRestServiceServer
                .bindTo(builder)
                .build();

        applicationServiceClient =
                new ApplicationServiceClient(builder);
    }

    @Test
    void shouldRegisterSubscriptionSuccessfully() {

        SubscriptionRequest request = createRequest();

        mockServer.expect(requestTo(
                        "http://localhost:8083/api/subscriptions"
                ))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.methodName").value("GetProducts"))
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.length()").value(2))
                .andRespond(withStatus(HttpStatus.CREATED));

        assertDoesNotThrow(() ->
                applicationServiceClient.registerSubscription(
                        "http://localhost:8083",
                        request
                )
        );

        mockServer.verify();
    }

    @Test
    void shouldThrowExceptionWhenApplicationServiceFails() {

        SubscriptionRequest request = createRequest();

        mockServer.expect(requestTo(
                        "http://localhost:8083/api/subscriptions"
                ))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        assertThrows(HttpServerErrorException.class, () ->
                applicationServiceClient.registerSubscription(
                        "http://localhost:8083",
                        request
                )
        );

        mockServer.verify();
    }

    private SubscriptionRequest createRequest() {
        return new SubscriptionRequest(
                "GetProducts",
                "test-token"
        );
    }
}
