package com.elfaddoui.backend.loyalty.dto;

public class LoyaltyInsightV2Response {
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
    private String riskLevel;
    private Integer daysToNextGift;
    private String recommendedAction;
    private Double confidence;

    public static LoyaltyInsightV2Response of(
            String action,
            String messageFr, String messageEn, String messageAr,
            String suggestionFr, String suggestionEn, String suggestionAr,
            String ctaFr, String ctaEn, String ctaAr,
            String riskLevel, Integer daysToNextGift, String recommendedAction, Double confidence
    ) {
        LoyaltyInsightV2Response response = new LoyaltyInsightV2Response();
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
        response.setRiskLevel(riskLevel);
        response.setDaysToNextGift(daysToNextGift);
        response.setRecommendedAction(recommendedAction);
        response.setConfidence(confidence);
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

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getDaysToNextGift() {
        return daysToNextGift;
    }

    public void setDaysToNextGift(Integer daysToNextGift) {
        this.daysToNextGift = daysToNextGift;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}
