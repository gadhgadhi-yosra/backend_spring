package com.elfaddoui.backend.ai.service.impl;

import com.elfaddoui.backend.ai.dto.CompetitorPriceQuote;
import com.elfaddoui.backend.ai.service.CompetitorPriceClient;
import com.elfaddoui.backend.product.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockCompetitorPriceClient implements CompetitorPriceClient {

    @Override
    public List<CompetitorPriceQuote> fetchQuotes(Product product) {
        double our = product.getPrice() == null ? 0.0 : product.getPrice().doubleValue();
        if (our <= 0) return List.of();

        return List.of(
                new CompetitorPriceQuote("Aziza", round2(our * 1.03), "https://example.com/aziza"),
                new CompetitorPriceQuote("Carrefour", round2(our * 0.98), "https://example.com/carrefour"),
                new CompetitorPriceQuote("Geant", round2(our * 1.01), "https://example.com/geant")
        );
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
