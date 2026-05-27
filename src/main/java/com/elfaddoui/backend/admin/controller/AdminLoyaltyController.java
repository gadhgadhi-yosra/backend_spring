package com.elfaddoui.backend.admin.controller;

import com.elfaddoui.backend.admin.dto.AdminLoyaltyAccountResponse;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyCustomerSummaryResponse;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyGiftRequest;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyPointAdjustmentRequest;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyVoucherRequest;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyGiftResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyVoucherResponse;
import com.elfaddoui.backend.loyalty.service.AdminLoyaltyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/admin/loyalty", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminLoyaltyController {

    private final AdminLoyaltyService adminLoyaltyService;

    public AdminLoyaltyController(AdminLoyaltyService adminLoyaltyService) {
        this.adminLoyaltyService = adminLoyaltyService;
    }

    @GetMapping("/customers/{customerId}")
    public AdminLoyaltyAccountResponse getCustomerAccount(@PathVariable Long customerId) {
        return adminLoyaltyService.getCustomerAccount(customerId);
    }

    @GetMapping("/customers")
    public List<AdminLoyaltyCustomerSummaryResponse> searchCustomers(
            @RequestParam(value = "query", required = false) String query
    ) {
        return adminLoyaltyService.searchCustomers(query);
    }

    @PostMapping("/customers/{customerId}/points")
    public AdminLoyaltyAccountResponse adjustPoints(
            @PathVariable Long customerId,
            @Valid @RequestBody AdminLoyaltyPointAdjustmentRequest request
    ) {
        return adminLoyaltyService.adjustPoints(customerId, request);
    }

    @PostMapping("/customers/{customerId}/vouchers")
    @ResponseStatus(HttpStatus.CREATED)
    public LoyaltyVoucherResponse createVoucher(
            @PathVariable Long customerId,
            @Valid @RequestBody AdminLoyaltyVoucherRequest request
    ) {
        return adminLoyaltyService.createVoucher(customerId, request);
    }

    @GetMapping("/gifts")
    public List<AdminLoyaltyGiftResponse> getGifts() {
        return adminLoyaltyService.getGifts();
    }

    @PostMapping("/gifts")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminLoyaltyGiftResponse createGift(@Valid @RequestBody AdminLoyaltyGiftRequest request) {
        return adminLoyaltyService.createGift(request);
    }

    @PutMapping("/gifts/{giftId}")
    public AdminLoyaltyGiftResponse updateGift(
            @PathVariable Long giftId,
            @Valid @RequestBody AdminLoyaltyGiftRequest request
    ) {
        return adminLoyaltyService.updateGift(giftId, request);
    }
}
