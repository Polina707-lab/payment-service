package com.payment.paymentmobileclient.controller;

import com.payment.paymentmobileclient.client.PaymentClient;
import com.payment.paymentmobileclient.dto.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Controller
public class ClientController {

    private final PaymentClient paymentClient;

    public ClientController(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/pay")
    public String pay(
            @RequestParam Long serviceId,
            @RequestParam String methodName,
            @RequestParam int days,
            Model model
    ) {
        try {
            Instant validFrom = Instant.now();
            Instant validTo = validFrom.plus(days, ChronoUnit.DAYS);

            PaymentRequestDto request = new PaymentRequestDto(serviceId, methodName, validFrom, validTo);
            PaymentResponseDto response = paymentClient.createPayment(request);

            model.addAttribute("token", response.token());
            model.addAttribute("status", response.status());
            model.addAttribute("subscriptionId", response.subscriptionId());
            model.addAttribute("serviceId", serviceId);
            model.addAttribute("methodName", methodName);
            
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при оплате: " + e.getMessage());
        }
        return "index";
    }

    @PostMapping("/validate")
    public String validate(
            @RequestParam Long serviceId,
            @RequestParam String methodName,
            @RequestParam String token,
            Model model
    ) {
        try {
            ValidationRequestDto request = new ValidationRequestDto(serviceId, methodName, token);
            ValidationResponseDto response = paymentClient.validateToken(request);
            
            model.addAttribute("verdict", response.verdict());
            model.addAttribute("checkedToken", token);
            
        } catch (Exception e) {
            model.addAttribute("validateError", "Ошибка при проверке: " + e.getMessage());
        }
        return "index";
    }
    

    @GetMapping("/subscriptions")
    public String subscriptions(Model model) {
        try {
            SubscriptionDto[] subs = paymentClient.getAllSubscriptions();
            model.addAttribute("subscriptions", subs);
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка загрузки данных: " + e.getMessage());
        }
        return "subscriptions";
    }
}