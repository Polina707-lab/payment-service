
package com.payment.paymentservice.controller;

import com.payment.paymentservice.dto.PaymentResponse;
import com.payment.paymentservice.exception.GlobalExceptionHandler;
import com.payment.paymentservice.exception.MethodNotFoundException;
import com.payment.paymentservice.service.PaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PaymentControllerWebTest {

    private MockMvc mockMvc;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);

        PaymentController controller =
                new PaymentController(paymentService);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnCreatedWhenPaymentSuccessful() throws Exception {

        when(paymentService.processPayment(any()))
                .thenReturn(new PaymentResponse(
                        1L,
                        "test-token",
                        "SUCCESS"
                ));

        String json = """
                {
                  "serviceId": 1,
                  "methodName": "GetProducts",
                  "validFrom": "2026-11-01T00:00:00Z",
                  "validTo": "2026-11-30T23:59:59Z"
                }
                """;

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subscriptionId").value(1))
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(paymentService).processPayment(any());
    }

    @Test
    void shouldRejectInvalidServiceId() throws Exception {

        String json = """
                {
                  "serviceId": 0,
                  "methodName": "GetProducts",
                  "validFrom": "2026-11-01T00:00:00Z",
                  "validTo": "2026-11-30T23:59:59Z"
                }
                """;

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(paymentService);
    }

    @Test
    void shouldReturnBadRequestForInvalidPeriod() throws Exception {

        String json = """
                {
                  "serviceId": 1,
                  "methodName": "GetProducts",
                  "validFrom": "2026-11-30T00:00:00Z",
                  "validTo": "2026-11-01T00:00:00Z"
                }
                """;

        when(paymentService.processPayment(any()))
                .thenThrow(new IllegalArgumentException(
                        "Дата окончания должна быть позже даты начала"
                ));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Дата окончания должна быть позже даты начала"));

        verify(paymentService).processPayment(any());
    }

    @Test
    void shouldReturnNotFoundForUnknownMethod() throws Exception {

        String json = """
                {
                  "serviceId": 1,
                  "methodName": "UnknownMethod",
                  "validFrom": "2026-11-01T00:00:00Z",
                  "validTo": "2026-11-30T23:59:59Z"
                }
                """;

        when(paymentService.processPayment(any()))
                .thenThrow(new MethodNotFoundException(
                        "Указанный сервис или метод не зарегистрирован"
                ));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(paymentService).processPayment(any());
    }
}
