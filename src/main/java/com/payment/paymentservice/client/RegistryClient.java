package com.payment.paymentservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class RegistryClient {

    private final RestClient restClient;

    public RegistryClient(
            RestClient.Builder builder,
            @Value("${registry.base-url}") String registryUrl
    ) {
        this.restClient = builder
                .baseUrl(registryUrl)
                .build();
    }

    public boolean methodExists(String serviceName, String methodName) {
        try {
            restClient.get()
                    .uri("/api/services/{serviceName}/methods/{methodName}",
                            serviceName, methodName)
                    .retrieve()
                    .toBodilessEntity();

            return true;

        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}