package com.payment.paymentservice.client;

import com.payment.paymentservice.dto.SubscriptionRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ApplicationServiceClient {

    private final RestClient restClient;
    private final JsonMapper jsonMapper;

    public ApplicationServiceClient(RestClient.Builder builder) {
        this.restClient = builder.build();
        this.jsonMapper = new JsonMapper();
    }

    public void registerSubscription(
            String serviceUrl,
            SubscriptionRequest request
    ) {
        String json = jsonMapper.writeValueAsString(request);

        restClient.post()
                .uri(serviceUrl + "/api/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .toBodilessEntity();
    }
}