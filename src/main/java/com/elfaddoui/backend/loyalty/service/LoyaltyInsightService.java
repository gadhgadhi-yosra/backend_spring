package com.elfaddoui.backend.loyalty.service;

import com.elfaddoui.backend.loyalty.dto.LoyaltyInsightRequest;
import com.elfaddoui.backend.loyalty.dto.LoyaltyInsightResponse;
import com.elfaddoui.backend.loyalty.dto.LoyaltyInsightV2Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class LoyaltyInsightService {
    @Value("${loyalty.insight.high-balance-threshold:2000}")
    private int highBalanceThreshold = 2000;

    public LoyaltyInsightResponse buildInsight(LoyaltyInsightRequest request) {
        int currentBalance = request.getCurrentBalance() == null ? 0 : Math.max(0, request.getCurrentBalance());

        if (request.getHistory() == null || request.getHistory().isEmpty()) {
            return LoyaltyInsightResponse.of(
                    "reset",
                    "Pas assez d'historique pour analyser ton rythme.",
                    "Not enough history to analyze your pace.",
                    "لا يوجد سجل كافٍ لتحليل الوتيرة.",
                    "Continue tes achats avec la carte fidélité pour débloquer des conseils personnalisés.",
                    "Keep shopping with your loyalty card to unlock personalized advice.",
                    "واصل الشراء ببطاقة الولاء للحصول على نصائح مخصصة.",
                    "Réinitialiser filtres",
                    "Reset filters",
                    "إعادة ضبط الفلاتر"
            );
        }

        WindowStats stats = computeLast30Stats(request);
        int earned = stats.earned();
        int used = stats.used();
        int count30 = stats.count30();

        if (count30 == 0) {
            return LoyaltyInsightResponse.of(
                    "reset",
                    "Aucun mouvement sur les 30 derniers jours.",
                    "No activity in the last 30 days.",
                    "لا توجد حركة خلال آخر 30 يومًا.",
                    "Reprends les achats fidélité pour relancer l'accumulation de points.",
                    "Resume loyalty purchases to restart points accumulation.",
                    "استأنف مشتريات الولاء لإعادة تجميع النقاط.",
                    "Voir tout",
                    "Show all",
                    "عرض الكل"
            );
        }

        int net = earned - used;
        if (currentBalance >= highBalanceThreshold) {
            return LoyaltyInsightResponse.of(
                    "showUsed30",
                    "Super: ton solde est élevé (" + currentBalance + " pts).",
                    "Great: your balance is high (" + currentBalance + " pts).",
                    "ممتاز: رصيدك مرتفع (" + currentBalance + " نقطة).",
                    "Profite d'un bon d'achat ou d'un cadeau pour éviter de laisser des points dormir.",
                    "Redeem a voucher or gift so points don't stay unused.",
                    "استفد من قسيمة أو هدية حتى لا تبقى النقاط غير مستعملة.",
                    "Voir points utilisés",
                    "View used points",
                    "عرض النقاط المستعملة"
            );
        }

        if (net >= 0) {
            return LoyaltyInsightResponse.of(
                    "showEarned30",
                    "Sur 30 jours: +" + earned + " gagnés, -" + used + " utilisés, net +" + net + ".",
                    "Over 30 days: +" + earned + " earned, -" + used + " used, net +" + net + ".",
                    "خلال 30 يومًا: +" + earned + " مكتسبة، -" + used + " مستعملة، الصافي +" + net + ".",
                    "Ton rythme est bon. Continue et vise un palier cadeau rapidement.",
                    "Your pace is good. Keep it up to reach the next gift tier quickly.",
                    "وتيرتك جيدة. واصل للوصول سريعًا إلى مستوى الهدية التالي.",
                    "Voir gains 30j",
                    "View 30d gains",
                    "عرض مكاسب 30 يومًا"
            );
        }

        return LoyaltyInsightResponse.of(
                "showEarned30",
                "Sur 30 jours: +" + earned + " gagnés, -" + used + " utilisés, net " + net + ".",
                "Over 30 days: +" + earned + " earned, -" + used + " used, net " + net + ".",
                "خلال 30 يومًا: +" + earned + " مكتسبة، -" + used + " مستعملة، الصافي " + net + ".",
                "Essaie de concentrer plus d'achats avec la carte pour reconstituer ton solde.",
                "Try concentrating more purchases with the loyalty card to rebuild your balance.",
                "حاول تركيز مزيد من المشتريات ببطاقة الولاء لإعادة بناء رصيدك.",
                "Voir gains 30j",
                "View 30d gains",
                "عرض مكاسب 30 يومًا"
        );
    }

    public LoyaltyInsightV2Response buildInsightV2(LoyaltyInsightRequest request) {
        LoyaltyInsightResponse base = buildInsight(request);
        int currentBalance = request.getCurrentBalance() == null ? 0 : Math.max(0, request.getCurrentBalance());
        WindowStats stats = computeLast30Stats(request);

        int earned = stats.earned();
        int used = stats.used();
        int count30 = stats.count30();
        int net = earned - used;

        String riskLevel;
        String recommendedAction;
        double confidence;
        Integer daysToNextGift;

        if (count30 == 0) {
            riskLevel = "medium";
            recommendedAction = "keep_earning";
            confidence = 0.62;
        } else if (currentBalance >= highBalanceThreshold) {
            riskLevel = "low";
            recommendedAction = "redeem_points";
            confidence = 0.86;
        } else if (net >= 0) {
            riskLevel = "low";
            recommendedAction = "keep_earning";
            confidence = 0.82;
        } else {
            riskLevel = "high";
            recommendedAction = "increase_earning_rate";
            confidence = 0.78;
        }

        if (currentBalance < highBalanceThreshold) {
            int missing = highBalanceThreshold - currentBalance;
            int avgDailyEarn = Math.max(1, earned / 30);
            daysToNextGift = (int) Math.ceil(missing / (double) avgDailyEarn);
            daysToNextGift = Math.min(daysToNextGift, 365);
        } else {
            daysToNextGift = 0;
        }

        return LoyaltyInsightV2Response.of(
                base.getAction(),
                base.getMessageFr(), base.getMessageEn(), base.getMessageAr(),
                base.getSuggestionFr(), base.getSuggestionEn(), base.getSuggestionAr(),
                base.getCtaFr(), base.getCtaEn(), base.getCtaAr(),
                riskLevel, daysToNextGift, recommendedAction, confidence
        );
    }

    private WindowStats computeLast30Stats(LoyaltyInsightRequest request) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        int earned = 0;
        int used = 0;
        int count30 = 0;

        if (request.getHistory() == null) {
            return new WindowStats(earned, used, count30);
        }

        for (LoyaltyInsightRequest.HistoryItem item : request.getHistory()) {
            if (item == null || item.getDate() == null || item.getPoints() == null) {
                continue;
            }
            OffsetDateTime date = parseClientDate(item.getDate());
            if (date == null) {
                continue;
            }
            long days = Duration.between(date, now).toDays();
            if (days >= 0 && days <= 30) {
                count30++;
                int points = item.getPoints();
                if (points > 0) {
                    earned += points;
                }
                if (points < 0) {
                    used += Math.abs(points);
                }
            }
        }
        return new WindowStats(earned, used, count30);
    }

    private OffsetDateTime parseClientDate(String raw) {
        try {
            return OffsetDateTime.parse(raw);
        } catch (Exception ignored) {
        }
        try {
            return Instant.parse(raw).atOffset(ZoneOffset.UTC);
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(raw).atOffset(ZoneOffset.UTC);
        } catch (Exception ignored) {
        }
        return null;
    }

    private record WindowStats(int earned, int used, int count30) {
    }
}
