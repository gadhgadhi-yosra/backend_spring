package com.elfaddoui.backend.ai.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PriceCompareResponse {
    private String productId;
    private double ourPrice;
    private List<Offer> offers = new ArrayList<>();
    private Instant updatedAt;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public double getOurPrice() {
        return ourPrice;
    }

    public void setOurPrice(double ourPrice) {
        this.ourPrice = ourPrice;
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Offer {
        private String storeName;
        private double price;
        private String url;

        public Offer() {
        }

        public Offer(String storeName, double price, String url) {
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

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}

