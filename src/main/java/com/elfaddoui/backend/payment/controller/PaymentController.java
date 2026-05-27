package com.elfaddoui.backend.payment.controller;

import com.elfaddoui.backend.exception.UnauthorizedException;
import com.elfaddoui.backend.payment.dto.PaymentIntentRequest;
import com.elfaddoui.backend.payment.dto.PaymentIntentResponse;
import com.elfaddoui.backend.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(
            value = {
                    "/api/payments/create-intent",
                    "/api/payments/create",
                    "/api/orders/payment-intent"
            },
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public PaymentIntentResponse createIntent(
            Authentication authentication,
            @Valid @RequestBody PaymentIntentRequest request
    ) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new UnauthorizedException("Unauthorized");
        }
        return paymentService.createIntent(authentication.getName(), request);
    }
}
