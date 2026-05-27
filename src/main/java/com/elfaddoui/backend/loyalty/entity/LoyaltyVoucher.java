package com.elfaddoui.backend.loyalty.entity;

import com.elfaddoui.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "loyalty_vouchers", indexes = {
        @Index(name = "idx_loyalty_voucher_account_expires", columnList = "account_id,expiresAt")
})
public class LoyaltyVoucher extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private LoyaltyAccount account;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 64, unique = true)
    private String code;

    @Column(nullable = false, length = 800)
    private String description = "";

    @Column(nullable = false)
    private Instant expiresAt;

    public LoyaltyVoucher() {
    }

    public LoyaltyVoucher(LoyaltyAccount account, String title, String code, String description, Instant expiresAt) {
        this.account = account;
        this.title = title;
        this.code = code;
        this.description = description == null ? "" : description;
        this.expiresAt = expiresAt;
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

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}

