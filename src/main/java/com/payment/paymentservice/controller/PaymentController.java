package com.payment.paymentservice.controller;

import com.payment.paymentservice.dto.PaymentRequest;
import com.payment.paymentservice.dto.PaymentResponse;
import com.payment.paymentservice.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments") //базовый URL - контроллера
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        return paymentService.processPayment(request);
    }
}
/*
Класс отвечает за:
- Приём POST /api/payments (принимает HTTP-запрос на оплату)
- Преобразование JSON в Java-объект PaymentRequest.
- Запуск валидации через @Valid.
- Вызов PaymentService (передача ему запроса)
- Возврат HTTP-ответа клиенту.
 */