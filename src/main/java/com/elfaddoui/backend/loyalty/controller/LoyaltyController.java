package com.elfaddoui.backend.loyalty.controller;

import com.elfaddoui.backend.loyalty.dto.LoyaltyGiftResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyHistoryResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyInsightRequest;
import com.elfaddoui.backend.loyalty.dto.LoyaltyInsightResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyInsightV2Response;
import com.elfaddoui.backend.loyalty.dto.LoyaltyMeResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyVoucherResponse;
import com.elfaddoui.backend.loyalty.service.LoyaltyInsightService;
import com.elfaddoui.backend.loyalty.service.LoyaltyService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/loyalty", produces = MediaType.APPLICATION_JSON_VALUE)
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final LoyaltyInsightService loyaltyInsightService;

    public LoyaltyController(LoyaltyService loyaltyService, LoyaltyInsightService loyaltyInsightService) {
        this.loyaltyService = loyaltyService;
        this.loyaltyInsightService = loyaltyInsightService;
    }

    @GetMapping("/me")
    public LoyaltyMeResponse me(Authentication authentication) {
        return loyaltyService.me(authentication.getName());
    }

    @GetMapping("/history")
    public List<LoyaltyHistoryResponse> history(Authentication authentication) {
        return loyaltyService.history(authentication.getName());
    }

    @GetMapping("/vouchers")
    public List<LoyaltyVoucherResponse> vouchers(Authentication authentication) {
        return loyaltyService.vouchers(authentication.getName());
    }

    @GetMapping("/gifts")
    public List<LoyaltyGiftResponse> gifts(Authentication authentication) {
        return loyaltyService.gifts(authentication.getName());
    }

    @PostMapping("/insights")
    public ResponseEntity<LoyaltyInsightResponse> insights(@RequestBody LoyaltyInsightRequest request) {
        return ResponseEntity.ok(loyaltyInsightService.buildInsight(request));
    }

    @PostMapping("/insights-v2")
    public ResponseEntity<LoyaltyInsightV2Response> insightsV2(@RequestBody LoyaltyInsightRequest request) {
        return ResponseEntity.ok(loyaltyInsightService.buildInsightV2(request));
    }
}
