package com.elfaddoui.backend.loyalty.entity;

import com.elfaddoui.backend.common.entity.AuditableEntity;
import com.elfaddoui.backend.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "loyalty_accounts", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class LoyaltyAccount extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "card_number", nullable = false, length = 32, unique = true)
    private String cardNumber;

    @Column(name = "points_balance", nullable = false)
    private int pointsBalance = 0;

    public LoyaltyAccount() {
    }

    public LoyaltyAccount(User user, String cardNumber) {
        this.user = user;
        this.cardNumber = cardNumber;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public int getPointsBalance() {
        return pointsBalance;
    }

    public void setPointsBalance(int pointsBalance) {
        this.pointsBalance = pointsBalance;
    }
}

