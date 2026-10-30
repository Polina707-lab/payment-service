package com.payment.paymentservice.controller;

import com.payment.paymentservice.dto.ValidationResponse;
import com.payment.paymentservice.exception.GlobalExceptionHandler;
import com.payment.paymentservice.service.ValidationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ValidationControllerWebTest {

    private MockMvc mockMvc;
    private ValidationService validationService;

    @BeforeEach
    void setUp() {

        validationService = mock(ValidationService.class);

        ValidationController controller =
                new ValidationController(validationService);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnOkForValidToken() throws Exception {

        when(validationService.validate(any()))
                .thenReturn(new ValidationResponse("OK"));

        String json = """
                {
                  "serviceID": 1,
                  "methodName": "GetProducts",
                  "token": "test-token"
                }
                """;

        mockMvc.perform(get("/api/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value("OK"));

        verify(validationService).validate(argThat(request ->
                request.serviceId().equals(1L)
                        && request.methodName().equals("GetProducts")
                        && request.token().equals("test-token")
        ));
    }

    @Test
    void shouldReturnExpiredForExpiredToken() throws Exception {

        when(validationService.validate(any()))
                .thenReturn(new ValidationResponse("EXPIRED"));

        String json = """
                {
                  "serviceID": 1,
                  "methodName": "GetProducts",
                  "token": "expired-token"
                }
                """;

        mockMvc.perform(get("/api/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value("EXPIRED"));

        verify(validationService).validate(any());
    }

    @Test
    void shouldReturnBadForInvalidToken() throws Exception {

        when(validationService.validate(any()))
                .thenReturn(new ValidationResponse("BAD"));

        String json = """
                {
                  "serviceID": 1,
                  "methodName": "GetProducts",
                  "token": "invalid-token"
                }
                """;

        mockMvc.perform(get("/api/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value("BAD"));

        verify(validationService).validate(any());
    }

    @Test
    void shouldRejectMissingServiceId() throws Exception {

        String json = """
                {
                  "methodName": "GetProducts",
                  "token": "test-token"
                }
                """;

        mockMvc.perform(get("/api/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(validationService);
    }

    @Test
    void shouldRejectEmptyToken() throws Exception {

        String json = """
                {
                  "serviceID": 1,
                  "methodName": "GetProducts",
                  "token": ""
                }
                """;

        mockMvc.perform(get("/api/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(validationService);
    }
}