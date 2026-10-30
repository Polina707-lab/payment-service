package com.payment.paymentservice.controller;

import com.payment.paymentservice.dto.ValidationRequest;
import com.payment.paymentservice.dto.ValidationResponse;
import com.payment.paymentservice.service.ValidationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValidationController {

    private final ValidationService validationService;

    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    @GetMapping("/api/validate")
    public ValidationResponse validate(
            @Valid @RequestBody ValidationRequest request
    ) {
        return validationService.validate(request);
    }
}