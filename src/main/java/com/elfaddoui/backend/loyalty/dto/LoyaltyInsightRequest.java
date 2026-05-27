package com.elfaddoui.backend.loyalty.dto;

import java.util.ArrayList;
import java.util.List;

public class LoyaltyInsightRequest {
    private Integer currentBalance;
    private List<HistoryItem> history = new ArrayList<>();

    public Integer getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(Integer currentBalance) {
        this.currentBalance = currentBalance;
    }

    public List<HistoryItem> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryItem> history) {
        this.history = history;
    }

    public static class HistoryItem {
        private String title;
        private String date;
        private Integer points;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Integer getPoints() {
            return points;
        }

        public void setPoints(Integer points) {
            this.points = points;
        }
    }
}
