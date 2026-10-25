package com.payment.paymentservice.client;

import com.payment.paymentservice.dto.SubscriptionRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ApplicationServiceClient {

    private final RestClient restClient;

    public ApplicationServiceClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public void registerSubscription(
            String serviceUrl, //базовый адрес прикладного сервиса
            SubscriptionRequest request //данные подписки, включая токен и оплаченный период
    ) {
        restClient.post()
                .uri(serviceUrl + "/api/subscriptions")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}