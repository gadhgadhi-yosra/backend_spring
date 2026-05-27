package com.elfaddoui.backend.order.controller;

import com.elfaddoui.backend.order.dto.CheckoutRequest;
import com.elfaddoui.backend.order.dto.CheckoutResponse;
import com.elfaddoui.backend.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/checkout", produces = MediaType.APPLICATION_JSON_VALUE)
public class CheckoutAliasController {

    private final OrderService orderService;

    public CheckoutAliasController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse create(Authentication authentication, @Valid @RequestBody CheckoutRequest request) {
        return orderService.create(authentication.getName(), request);
    }
}
