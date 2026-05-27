package com.elfaddoui.backend.loyalty.dto;

public class LoyaltyInsightResponse {
    private String action;
    private String messageFr;
    private String messageEn;
    private String messageAr;
    private String suggestionFr;
    private String suggestionEn;
    private String suggestionAr;
    private String ctaFr;
    private String ctaEn;
    private String ctaAr;

    public static LoyaltyInsightResponse of(
            String action,
            String messageFr, String messageEn, String messageAr,
            String suggestionFr, String suggestionEn, String suggestionAr,
            String ctaFr, String ctaEn, String ctaAr
    ) {
        LoyaltyInsightResponse response = new LoyaltyInsightResponse();
        response.setAction(action);
        response.setMessageFr(messageFr);
        response.setMessageEn(messageEn);
        response.setMessageAr(messageAr);
        response.setSuggestionFr(suggestionFr);
        response.setSuggestionEn(suggestionEn);
        response.setSuggestionAr(suggestionAr);
        response.setCtaFr(ctaFr);
        response.setCtaEn(ctaEn);
        response.setCtaAr(ctaAr);
        return response;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMessageFr() {
        return messageFr;
    }

    public void setMessageFr(String messageFr) {
        this.messageFr = messageFr;
    }

    public String getMessageEn() {
        return messageEn;
    }

    public void setMessageEn(String messageEn) {
        this.messageEn = messageEn;
    }

    public String getMessageAr() {
        return messageAr;
    }

    public void setMessageAr(String messageAr) {
        this.messageAr = messageAr;
    }

    public String getSuggestionFr() {
        return suggestionFr;
    }

    public void setSuggestionFr(String suggestionFr) {
        this.suggestionFr = suggestionFr;
    }

    public String getSuggestionEn() {
        return suggestionEn;
    }

    public void setSuggestionEn(String suggestionEn) {
        this.suggestionEn = suggestionEn;
    }

    public String getSuggestionAr() {
        return suggestionAr;
    }

    public void setSuggestionAr(String suggestionAr) {
        this.suggestionAr = suggestionAr;
    }

    public String getCtaFr() {
        return ctaFr;
    }

    public void setCtaFr(String ctaFr) {
        this.ctaFr = ctaFr;
    }

    public String getCtaEn() {
        return ctaEn;
    }

    public void setCtaEn(String ctaEn) {
        this.ctaEn = ctaEn;
    }

    public String getCtaAr() {
        return ctaAr;
    }

    public void setCtaAr(String ctaAr) {
        this.ctaAr = ctaAr;
    }
}
