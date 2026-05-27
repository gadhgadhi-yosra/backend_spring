package com.elfaddoui.backend.loyalty.repository;

import com.elfaddoui.backend.loyalty.entity.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, Long> {
    Optional<LoyaltyAccount> findByUserId(Long userId);
    List<LoyaltyAccount> findByUserIdIn(List<Long> userIds);
    Optional<LoyaltyAccount> findByUserEmailIgnoreCase(String email);
    boolean existsByCardNumber(String cardNumber);
}
