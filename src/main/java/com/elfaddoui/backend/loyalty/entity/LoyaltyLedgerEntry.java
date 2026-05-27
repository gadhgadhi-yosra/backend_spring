package com.elfaddoui.backend.loyalty.entity;

import com.elfaddoui.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "loyalty_ledger_entries", indexes = {
        @Index(name = "idx_loyalty_ledger_account_created", columnList = "account_id,createdAt")
})
public class LoyaltyLedgerEntry extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private LoyaltyAccount account;

    @Column(nullable = false, length = 240)
    private String title;

    @Column(nullable = false)
    private int points;

    public LoyaltyLedgerEntry() {
    }

    public LoyaltyLedgerEntry(LoyaltyAccount account, String title, int points) {
        this.account = account;
        this.title = title;
        this.points = points;
    }

    public Long getId() {
        return id;
    }

    public LoyaltyAccount getAccount() {
        return account;
    }

    public String getTitle() {
        return title;
    }

    public int getPoints() {
        return points;
    }
}

