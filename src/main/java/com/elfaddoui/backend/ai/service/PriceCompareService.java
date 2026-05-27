package com.elfaddoui.backend.ai.service;

import com.elfaddoui.backend.ai.dto.PriceCompareResponse;

public interface PriceCompareService {
    PriceCompareResponse compare(Long productId);
}

