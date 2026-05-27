package com.elfaddoui.backend.ai.service.impl;

import com.elfaddoui.backend.ai.dto.CompetitorPriceQuote;
import com.elfaddoui.backend.ai.dto.PriceCompareResponse;
import com.elfaddoui.backend.ai.service.CompetitorPriceClient;
import com.elfaddoui.backend.ai.service.PriceCompareService;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DefaultPriceCompareService implements PriceCompareService {

    private final ProductRepository productRepository;
    private final CompetitorPriceClient competitorPriceClient;

    public DefaultPriceCompareService(ProductRepository productRepository,
                                      CompetitorPriceClient competitorPriceClient) {
        this.productRepository = productRepository;
        this.competitorPriceClient = competitorPriceClient;
    }

    @Override
    public PriceCompareResponse compare(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        double ourPrice = product.getPrice() == null ? 0.0 : product.getPrice().doubleValue();

        List<CompetitorPriceQuote> quotes = competitorPriceClient.fetchQuotes(product);
        List<PriceCompareResponse.Offer> offers = quotes.stream()
                .filter(q -> q.getStoreName() != null && !q.getStoreName().isBlank())
                .filter(q -> q.getPrice() != null && q.getPrice() > 0)
                .map(q -> new PriceCompareResponse.Offer(q.getStoreName(), q.getPrice(), q.getUrl()))
                .sorted((a, b) -> Double.compare(a.getPrice(), b.getPrice()))
                .collect(Collectors.toList());

        PriceCompareResponse response = new PriceCompareResponse();
        response.setProductId(String.valueOf(productId));
        response.setOurPrice(ourPrice);
        response.setOffers(offers);
        response.setUpdatedAt(Instant.now());
        return response;
    }
}
