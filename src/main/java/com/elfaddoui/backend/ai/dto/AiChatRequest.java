package com.elfaddoui.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class AiChatRequest {
    @NotBlank
    private String message;
    private String locale;
    private Context context;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
    public Context getContext() { return context; }
    public void setContext(Context context) { this.context = context; }

    public static class Context {
        private Integer loyaltyBalance;
        private List<String> cartProductIds;
        private List<String> favoriteProductIds;

        public Integer getLoyaltyBalance() { return loyaltyBalance; }
        public void setLoyaltyBalance(Integer loyaltyBalance) { this.loyaltyBalance = loyaltyBalance; }
        public List<String> getCartProductIds() { return cartProductIds; }
        public void setCartProductIds(List<String> cartProductIds) { this.cartProductIds = cartProductIds; }
        public List<String> getFavoriteProductIds() { return favoriteProductIds; }
        public void setFavoriteProductIds(List<String> favoriteProductIds) { this.favoriteProductIds = favoriteProductIds; }
    }
}
