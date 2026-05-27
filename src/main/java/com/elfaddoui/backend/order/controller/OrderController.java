package com.elfaddoui.backend.order.controller;

import com.elfaddoui.backend.order.dto.CheckoutRequest;
import com.elfaddoui.backend.order.dto.CheckoutResponse;
import com.elfaddoui.backend.order.dto.DeliverySettingsResponse;
import com.elfaddoui.backend.order.dto.OrderDetailsResponse;
import com.elfaddoui.backend.order.dto.DeliveryTrackingResponse;
import com.elfaddoui.backend.order.dto.OrderHistoryItemResponse;
import com.elfaddoui.backend.order.service.OrderEventService;
import com.elfaddoui.backend.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

    private final OrderService orderService;
    private final OrderEventService orderEventService;

    public OrderController(OrderService orderService, OrderEventService orderEventService) {
        this.orderService = orderService;
        this.orderEventService = orderEventService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse create(Authentication authentication, @Valid @RequestBody CheckoutRequest request) {
        return orderService.create(authentication.getName(), request);
    }

    @PostMapping(value = "/checkout", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CheckoutResponse createCheckout(Authentication authentication, @Valid @RequestBody CheckoutRequest request) {
        return orderService.create(authentication.getName(), request);
    }

    @GetMapping("/latest")
    public DeliveryTrackingResponse latest(Authentication authentication) {
        return orderService.getLatestTracking(authentication.getName());
    }

    @GetMapping("/delivery/settings")
    public DeliverySettingsResponse deliverySettings() {
        return orderService.getDeliverySettings();
    }

    @GetMapping("/history")
    public List<OrderHistoryItemResponse> history(Authentication authentication) {
        return orderService.getHistory(authentication.getName());
    }

    @GetMapping("/{orderReference}/details")
    public OrderDetailsResponse details(Authentication authentication, @PathVariable String orderReference) {
        return orderService.getDetails(authentication.getName(), orderReference);
    }

    @PatchMapping("/{orderReference}/cancel")
    public OrderHistoryItemResponse cancel(Authentication authentication, @PathVariable String orderReference) {
        return orderService.cancel(authentication.getName(), orderReference);
    }

    @GetMapping("/{orderReference}")
    public DeliveryTrackingResponse tracking(Authentication authentication, @PathVariable String orderReference) {
        return orderService.getTracking(authentication.getName(), orderReference);
    }

    @GetMapping("/{orderReference}/tracking")
    public DeliveryTrackingResponse trackingAlias(Authentication authentication, @PathVariable String orderReference) {
        return orderService.getTracking(authentication.getName(), orderReference);
    }

    @GetMapping(value = {"/{orderReference}/invoice", "/{orderReference}/facture"}, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> invoice(Authentication authentication, @PathVariable String orderReference) {
        byte[] pdf = orderService.generateInvoicePdf(authentication.getName(), orderReference);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice-" + orderReference + ".pdf")
                .body(pdf);
    }

    @GetMapping({"/{orderReference}/invoice-url", "/{orderReference}/invoice/link"})
    public Map<String, String> invoiceUrl(Authentication authentication, @PathVariable String orderReference) {
        String url = orderService.getInvoiceUrl(authentication.getName(), orderReference);
        return Map.of("url", url);
    }

    @GetMapping(value = "/{orderReference}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(Authentication authentication, @PathVariable String orderReference) {
        DeliveryTrackingResponse tracking = orderService.getTracking(authentication.getName(), orderReference);
        return orderEventService.subscribe(authentication.getName(), orderReference, tracking);
    }
}
