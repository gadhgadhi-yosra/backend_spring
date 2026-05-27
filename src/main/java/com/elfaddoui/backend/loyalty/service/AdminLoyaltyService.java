package com.elfaddoui.backend.loyalty.service;

import com.elfaddoui.backend.admin.dto.AdminLoyaltyAccountResponse;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyCustomerSummaryResponse;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyGiftRequest;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyPointAdjustmentRequest;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyVoucherRequest;
import com.elfaddoui.backend.admin.dto.AdminLoyaltyGiftResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyVoucherResponse;

import java.util.List;

public interface AdminLoyaltyService {
    AdminLoyaltyAccountResponse getCustomerAccount(Long customerId);
    List<AdminLoyaltyCustomerSummaryResponse> searchCustomers(String query);
    AdminLoyaltyAccountResponse adjustPoints(Long customerId, AdminLoyaltyPointAdjustmentRequest request);
    LoyaltyVoucherResponse createVoucher(Long customerId, AdminLoyaltyVoucherRequest request);
    List<AdminLoyaltyGiftResponse> getGifts();
    AdminLoyaltyGiftResponse createGift(AdminLoyaltyGiftRequest request);
    AdminLoyaltyGiftResponse updateGift(Long giftId, AdminLoyaltyGiftRequest request);
}
