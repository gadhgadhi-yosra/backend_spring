package com.elfaddoui.backend.loyalty.config;

import com.elfaddoui.backend.loyalty.entity.LoyaltyGift;
import com.elfaddoui.backend.loyalty.repository.LoyaltyGiftRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class LoyaltyGiftSeeder {

    @Bean
    CommandLineRunner seedLoyaltyGifts(LoyaltyGiftRepository giftRepository) {
        return args -> {
            seedGift(giftRepository, "Café gratuit", 800, 10);
            seedGift(giftRepository, "Sac réutilisable", 1200, 20);
            seedGift(giftRepository, "Box découverte", 2500, 30);
            seedGift(giftRepository, "Bon d'achat 20 DT", 4000, 40);
        };
    }

    private void seedGift(LoyaltyGiftRepository giftRepository, String title, int points, int sortOrder) {
        if (giftRepository.existsByTitle(title)) {
            return;
        }
        LoyaltyGift gift = new LoyaltyGift(title, points);
        gift.setSortOrder(sortOrder);
        giftRepository.save(gift);
    }
}
