package com.elfaddoui.backend.ai.dto;

import java.util.ArrayList;
import java.util.List;

public class BudgetAssistantResponse {
    private String message;
    private double budget;
    private double estimatedTotal;
    private double estimatedSavings;
    private List<Item> items = new ArrayList<>();
    private List<Substitution> substitutions = new ArrayList<>();
    private List<AiChatResponse.Action> actions = new ArrayList<>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double getEstimatedTotal() {
        return estimatedTotal;
    }

    public void setEstimatedTotal(double estimatedTotal) {
        this.estimatedTotal = estimatedTotal;
    }

    public double getEstimatedSavings() {
        return estimatedSavings;
    }

    public void setEstimatedSavings(double estimatedSavings) {
        this.estimatedSavings = estimatedSavings;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Substitution> getSubstitutions() {
        return substitutions;
    }

    public void setSubstitutions(List<Substitution> substitutions) {
        this.substitutions = substitutions;
    }

    public List<AiChatResponse.Action> getActions() {
        return actions;
    }

    public void setActions(List<AiChatResponse.Action> actions) {
        this.actions = actions;
    }

    public static class Item {
        private Long productId;
        private String name;
        private String imageUrl;
        private String category;
        private double price;
        private String reason;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class Substitution {
        private String originalKeyword;
        private Long suggestedProductId;
        private String suggestedName;
        private double price;
        private String reason;

        public String getOriginalKeyword() {
            return originalKeyword;
        }

        public void setOriginalKeyword(String originalKeyword) {
            this.originalKeyword = originalKeyword;
        }

        public Long getSuggestedProductId() {
            return suggestedProductId;
        }

        public void setSuggestedProductId(Long suggestedProductId) {
            this.suggestedProductId = suggestedProductId;
        }

        public String getSuggestedName() {
            return suggestedName;
        }

        public void setSuggestedName(String suggestedName) {
            this.suggestedName = suggestedName;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
