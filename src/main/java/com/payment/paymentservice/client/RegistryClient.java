package com.payment.paymentservice.client;

import com.payment.paymentservice.dto.RegistryMethodResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

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

    public Optional<RegistryMethodResponse> findMethod(
            String serviceName,
            String methodName
    ) {
        try {
            RegistryMethodResponse response = restClient.get()
                    .uri("/api/services/{serviceName}/methods/{methodName}",
                            serviceName, methodName)
                    .retrieve()
                    .body(RegistryMethodResponse.class);

            return Optional.ofNullable(response);

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}