package com.payment.paymentmobileclient.client;

import com.payment.paymentmobileclient.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentClient {

    private final RestClient restClient;

    public PaymentClient(@Value("${payment.service.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public PaymentResponseDto createPayment(PaymentRequestDto request) {
        return restClient.post()
                .uri("/api/payments")
                .body(request)
                .retrieve()
                .body(PaymentResponseDto.class);
    }

    public ValidationResponseDto validateToken(ValidationRequestDto request) {
        return restClient.method(HttpMethod.GET)
                .uri("/api/validate")
                .body(request)
                .retrieve()
                .body(ValidationResponseDto.class);
    }

    public SubscriptionDto[] getAllSubscriptions() {
        return restClient.get()
                .uri("/api/payments")
                .retrieve()
                .body(SubscriptionDto[].class);
    }
}