package com.elfaddoui.backend.ai.dto;

public class CompetitorPriceQuote {
    private String storeName;
    private Double price;
    private String url;

    public CompetitorPriceQuote() {
    }

    public CompetitorPriceQuote(String storeName, Double price, String url) {
        this.storeName = storeName;
        this.price = price;
        this.url = url;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
