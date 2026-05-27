package com.elfaddoui.backend.loyalty.service;

import com.elfaddoui.backend.loyalty.dto.LoyaltyGiftResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyHistoryResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyMeResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyVoucherResponse;

import java.util.List;

public interface LoyaltyService {
    LoyaltyMeResponse me(String userEmail);
    List<LoyaltyHistoryResponse> history(String userEmail);
    List<LoyaltyVoucherResponse> vouchers(String userEmail);
    List<LoyaltyGiftResponse> gifts(String userEmail);
}
