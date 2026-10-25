package com.payment.paymentservice.exception;

public class MethodNotFoundException extends RuntimeException {

    public MethodNotFoundException(String message) {
        super(message);
    }
}