package com.elfaddoui.backend.loyalty.repository;

import com.elfaddoui.backend.loyalty.entity.LoyaltyGift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoyaltyGiftRepository extends JpaRepository<LoyaltyGift, Long> {
    List<LoyaltyGift> findByActiveTrueOrderBySortOrderAscPointsAsc();
    List<LoyaltyGift> findAllByOrderBySortOrderAscPointsAsc();
    Optional<LoyaltyGift> findFirstByActiveTrueAndPointsGreaterThanOrderByPointsAsc(int points);
    boolean existsByTitle(String title);
}
