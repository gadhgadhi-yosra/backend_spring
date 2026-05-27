package com.elfaddoui.backend.loyalty.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "loyalty_gifts")
public class LoyaltyGift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    public LoyaltyGift() {
    }

    public LoyaltyGift(String title, int points) {
        this.title = title;
        this.points = points;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getPoints() {
        return points;
    }

    public boolean isActive() {
        return active;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}

