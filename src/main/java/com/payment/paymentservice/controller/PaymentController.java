package com.payment.paymentservice.controller;

import com.payment.paymentservice.dto.PaymentRequest;
import com.payment.paymentservice.dto.PaymentResponse;
import com.payment.paymentservice.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.payment.paymentservice.entity.Subscription;
import com.payment.paymentservice.repository.SubscriptionRepository;
import java.util.List;

@RestController
@RequestMapping("/api/payments") //базовый URL - контроллера
public class PaymentController {

    private final PaymentService paymentService;
    private final SubscriptionRepository subscriptionRepository;

    public PaymentController(PaymentService paymentService, SubscriptionRepository subscriptionRepository) {
        this.paymentService = paymentService;
        this.subscriptionRepository = subscriptionRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(
            @Valid @RequestBody PaymentRequest request
    ) {
        return paymentService.processPayment(request);
    }
    
    @GetMapping
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }
}
/*
Класс отвечает за:
- Приём POST /api/payments (принимает HTTP-запрос на оплату)
- Преобразование JSON в Java-объект PaymentRequest.
- Запуск валидации через @Valid.
- Вызов PaymentService (передача ему запроса)
- Возврат HTTP-ответа клиенту.

- Приём GET /api/payments (просмотр всех подписок).
- Возвращает список всех оформленных подписок из БД.
- Используется мобильным приложением для просмотра актуальных данных.
*/