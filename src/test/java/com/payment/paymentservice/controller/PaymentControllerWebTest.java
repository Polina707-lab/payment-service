package com.payment.paymentservice.controller;

import com.payment.paymentservice.dto.PaymentResponse;
import com.payment.paymentservice.service.PaymentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

        var validator =
                new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
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
                  "companyName": "CompanyA",
                  "serviceName": "ProductService",
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
    void shouldRejectBlankCompanyName() throws Exception {

        String json = """
            {
              "companyName": "",
              "serviceName": "ProductService",
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
}