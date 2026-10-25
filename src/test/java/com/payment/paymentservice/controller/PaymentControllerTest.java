package com.payment.paymentservice.controller;

import com.payment.paymentservice.dto.PaymentRequest;
import com.payment.paymentservice.dto.PaymentResponse;
import com.payment.paymentservice.service.PaymentService;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @Test
    void shouldCreatePayment() {

        PaymentService paymentService = mock(PaymentService.class);

        PaymentController controller =
                new PaymentController(paymentService);

        PaymentRequest request = new PaymentRequest(
                "CompanyA",
                "ProductService",
                "GetProducts",
                Instant.parse("2026-11-01T00:00:00Z"),
                Instant.parse("2026-11-30T23:59:59Z")
        );

        PaymentResponse expectedResponse = new PaymentResponse(
                1L,
                "test-token",
                "SUCCESS"
        );

        when(paymentService.processPayment(request))
                .thenReturn(expectedResponse);

        PaymentResponse actualResponse =
                controller.createPayment(request);

        assertEquals(expectedResponse, actualResponse);

        verify(paymentService).processPayment(request);
    }
}