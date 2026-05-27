package com.elfaddoui.backend;

import com.elfaddoui.backend.loyalty.dto.LoyaltyInsightRequest;
import com.elfaddoui.backend.loyalty.dto.LoyaltyInsightResponse;
import com.elfaddoui.backend.loyalty.service.LoyaltyInsightService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoyaltyInsightServiceTest {

    private final LoyaltyInsightService service = new LoyaltyInsightService();

    @Test
    void returnsResetWhenHistoryIsEmpty() {
        LoyaltyInsightRequest request = new LoyaltyInsightRequest();
        request.setCurrentBalance(0);
        request.setHistory(List.of());

        LoyaltyInsightResponse response = service.buildInsight(request);
        assertEquals("reset", response.getAction());
    }

    @Test
    void returnsShowUsed30WhenBalanceIsHigh() {
        LoyaltyInsightRequest request = new LoyaltyInsightRequest();
        request.setCurrentBalance(2500);
        request.setHistory(List.of(item(20), item(-10)));

        LoyaltyInsightResponse response = service.buildInsight(request);
        assertEquals("showUsed30", response.getAction());
    }

    @Test
    void returnsShowEarned30WhenNetIsPositive() {
        LoyaltyInsightRequest request = new LoyaltyInsightRequest();
        request.setCurrentBalance(120);
        request.setHistory(List.of(item(60), item(-10)));

        LoyaltyInsightResponse response = service.buildInsight(request);
        assertEquals("showEarned30", response.getAction());
    }

    @Test
    void acceptsDateWithoutTimezoneFromClient() {
        LoyaltyInsightRequest request = new LoyaltyInsightRequest();
        request.setCurrentBalance(120);

        LoyaltyInsightRequest.HistoryItem item = new LoyaltyInsightRequest.HistoryItem();
        item.setTitle("tx");
        item.setPoints(30);
        item.setDate(LocalDateTime.now(ZoneOffset.UTC).minusDays(2).toString());
        request.setHistory(List.of(item));

        LoyaltyInsightResponse response = service.buildInsight(request);
        assertEquals("showEarned30", response.getAction());
    }

    @Test
    void ignoresFutureDate() {
        LoyaltyInsightRequest request = new LoyaltyInsightRequest();
        request.setCurrentBalance(120);

        LoyaltyInsightRequest.HistoryItem future = new LoyaltyInsightRequest.HistoryItem();
        future.setTitle("future");
        future.setPoints(50);
        future.setDate(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1).toString());
        request.setHistory(List.of(future));

        LoyaltyInsightResponse response = service.buildInsight(request);
        assertEquals("reset", response.getAction());
    }

    private LoyaltyInsightRequest.HistoryItem item(int points) {
        LoyaltyInsightRequest.HistoryItem item = new LoyaltyInsightRequest.HistoryItem();
        item.setTitle("tx");
        item.setPoints(points);
        item.setDate(OffsetDateTime.now(ZoneOffset.UTC).minusDays(2).toString());
        return item;
    }
}
