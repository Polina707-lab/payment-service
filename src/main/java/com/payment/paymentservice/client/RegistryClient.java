
package com.payment.paymentservice.client;

import com.payment.paymentservice.dto.RegistryMethodResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

// Отправляет HTTP GET в Registry Service и получает информацию о методе
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
            Long serviceId,
            String methodName
    ) {
        try {
            RegistryMethodResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/services")
                            .queryParam("serviceId", serviceId)
                            .queryParam("methodName", methodName)
                            .build())
                    .retrieve()
                    .body(RegistryMethodResponse.class);

            return Optional.ofNullable(response);

        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
