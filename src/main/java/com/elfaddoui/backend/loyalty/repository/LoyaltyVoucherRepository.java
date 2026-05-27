package com.elfaddoui.backend.loyalty.repository;

import com.elfaddoui.backend.loyalty.entity.LoyaltyAccount;
import com.elfaddoui.backend.loyalty.entity.LoyaltyVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoyaltyVoucherRepository extends JpaRepository<LoyaltyVoucher, Long> {
    List<LoyaltyVoucher> findByAccountIdOrderByExpiresAtAsc(Long accountId);
    List<LoyaltyVoucher> findByAccountOrderByExpiresAtAsc(LoyaltyAccount account);
}
