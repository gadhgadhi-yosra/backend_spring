package com.elfaddoui.backend.ai.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class BudgetAssistantRequest {
    @NotNull
    @DecimalMin("1.0")
    private Double budget;
    private String locale = "fr";
    private String message;
    private List<Long> cartProductIds = List.of();

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Long> getCartProductIds() {
        return cartProductIds;
    }

    public void setCartProductIds(List<Long> cartProductIds) {
        this.cartProductIds = cartProductIds == null ? List.of() : cartProductIds;
    }
}
