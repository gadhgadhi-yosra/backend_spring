package com.elfaddoui.backend.loyalty.repository;

import com.elfaddoui.backend.loyalty.entity.LoyaltyLedgerEntry;
import com.elfaddoui.backend.loyalty.entity.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface LoyaltyLedgerEntryRepository extends JpaRepository<LoyaltyLedgerEntry, Long> {
    List<LoyaltyLedgerEntry> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    List<LoyaltyLedgerEntry> findByAccountOrderByCreatedAtDesc(LoyaltyAccount account);
    List<LoyaltyLedgerEntry> findByAccountAndCreatedAtBetween(LoyaltyAccount account, Instant from, Instant to);

    @Query("""
            select coalesce(sum(e.points), 0)
            from LoyaltyLedgerEntry e
            where e.account.id = :accountId
              and e.createdAt >= :start
              and e.createdAt < :end
              and e.points > 0
            """)
    Long sumEarnedThisMonth(@Param("accountId") Long accountId, @Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            select coalesce(sum(e.points), 0)
            from LoyaltyLedgerEntry e
            where e.account.id = :accountId
              and e.createdAt >= :start
              and e.createdAt < :end
              and e.points < 0
            """)
    Long sumUsedThisMonth(@Param("accountId") Long accountId, @Param("start") Instant start, @Param("end") Instant end);
}
