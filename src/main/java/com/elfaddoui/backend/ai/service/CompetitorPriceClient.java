package com.elfaddoui.backend.ai.service;

import com.elfaddoui.backend.ai.dto.CompetitorPriceQuote;
import com.elfaddoui.backend.product.entity.Product;

import java.util.List;

public interface CompetitorPriceClient {
    List<CompetitorPriceQuote> fetchQuotes(Product product);
}
