package com.elfaddoui.backend;

import com.elfaddoui.backend.loyalty.entity.LoyaltyAccount;
import com.elfaddoui.backend.loyalty.entity.LoyaltyGift;
import com.elfaddoui.backend.loyalty.entity.LoyaltyLedgerEntry;
import com.elfaddoui.backend.loyalty.entity.LoyaltyVoucher;
import com.elfaddoui.backend.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoyaltyIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void loyaltyEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/loyalty/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void meCreatesAccountAutomaticallyAndReturnsFrontendContract() throws Exception {
        seedDefaultGifts();
        String token = clientToken("loyalty-auto@elfaddoui.test");

        mockMvc.perform(get("/api/loyalty/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsBalance", is(0)))
                .andExpect(jsonPath("$.cardNumber", startsWith("201")))
                .andExpect(jsonPath("$.earnedThisMonth", is(0)))
                .andExpect(jsonPath("$.usedThisMonth", is(0)))
                .andExpect(jsonPath("$.nextGiftPoints", is(800)));
    }

    @Test
    void loyaltySummaryHistoryVouchersAndGiftsMatchFrontendShapes() throws Exception {
        seedDefaultGifts();
        String token = clientToken("loyalty-full@elfaddoui.test");
        User user = userRepository.findByEmail("loyalty-full@elfaddoui.test").orElseThrow();
        LoyaltyAccount account = new LoyaltyAccount(user, "2010000125063670");
        account.setPointsBalance(2480);
        account = loyaltyAccountRepository.save(account);

        loyaltyLedgerEntryRepository.save(new LoyaltyLedgerEntry(account, "Achat magasin", 45));
        loyaltyLedgerEntryRepository.save(new LoyaltyLedgerEntry(account, "Bon utilise", -300));
        loyaltyVoucherRepository.save(new LoyaltyVoucher(
                account,
                "-15% sur Boissons",
                "DRINK15",
                "Valable sur boissons",
                Instant.parse("2026-05-20T23:59:59Z")
        ));

        mockMvc.perform(get("/api/loyalty/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pointsBalance", is(2480)))
                .andExpect(jsonPath("$.cardNumber", is("2010000125063670")))
                .andExpect(jsonPath("$.earnedThisMonth", is(45)))
                .andExpect(jsonPath("$.usedThisMonth", is(300)))
                .andExpect(jsonPath("$.nextGiftPoints", is(20)));

        mockMvc.perform(get("/api/loyalty/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].points").exists());

        mockMvc.perform(get("/api/loyalty/vouchers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("-15% sur Boissons")))
                .andExpect(jsonPath("$[0].code", is("DRINK15")))
                .andExpect(jsonPath("$[0].description", is("Valable sur boissons")))
                .andExpect(jsonPath("$[0].expiresAt", is("2026-05-20T23:59:59Z")));

        mockMvc.perform(get("/api/loyalty/gifts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(4))))
                .andExpect(jsonPath("$[0].title", is("Café gratuit")))
                .andExpect(jsonPath("$[0].points", is(800)))
                .andExpect(jsonPath("$[0].unlocked", is(true)))
                .andExpect(jsonPath("$[2].title", is("Box découverte")))
                .andExpect(jsonPath("$[2].points", is(2500)))
                .andExpect(jsonPath("$[2].unlocked", is(false)));
    }

    private void seedDefaultGifts() {
        seedGift("Café gratuit", 800, 10);
        seedGift("Sac réutilisable", 1200, 20);
        seedGift("Box découverte", 2500, 30);
        seedGift("Bon d'achat 20 DT", 4000, 40);
    }

    private void seedGift(String title, int points, int sortOrder) {
        LoyaltyGift gift = new LoyaltyGift(title, points);
        gift.setSortOrder(sortOrder);
        loyaltyGiftRepository.save(gift);
    }
}
