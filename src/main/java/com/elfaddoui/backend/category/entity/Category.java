package com.elfaddoui.backend.category.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "category_key", nullable = false, unique = true)
    private String key;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "image_url", length = 1200)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "featured_promo", nullable = false)
    private boolean promo = false;

    @Column(name = "featured_bio", nullable = false)
    private boolean bio = false;

    @Column(name = "featured_new", nullable = false)
    private boolean isNew = false;

    @Column(name = "featured_popular", nullable = false)
    private boolean popular = false;

    @Column(name = "custom_tags", length = 2000)
    private String customTags;

    public Category() {
    }

    public Category(String name, boolean active) {
        this.name = name;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPromo() {
        return promo;
    }

    public boolean isBio() {
        return bio;
    }

    public boolean isNew() {
        return isNew;
    }

    public boolean isPopular() {
        return popular;
    }

    public String getCustomTags() {
        return customTags;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setPromo(boolean promo) {
        this.promo = promo;
    }

    public void setBio(boolean bio) {
        this.bio = bio;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public void setPopular(boolean popular) {
        this.popular = popular;
    }

    public void setCustomTags(String customTags) {
        this.customTags = customTags;
    }
}
