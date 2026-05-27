package com.elfaddoui.backend.product.entity;

import com.elfaddoui.backend.category.entity.Category;
import com.elfaddoui.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal oldPrice;

    @Column(nullable = false)
    private Integer discountPct = 0;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 1200)
    private String imageUrl;

    @Column(nullable = false)
    private Integer stockQty;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Double rating = 0.0;

    @Column(nullable = false)
    private Long salesCount = 0L;

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

    @Column(name = "promo_label", length = 160)
    private String promoLabel;

    @Column(name = "promo_starts_at")
    private Instant promoStartsAt;

    @Column(name = "promo_ends_at")
    private Instant promoEndsAt;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }

    public Integer getDiscountPct() {
        return discountPct;
    }

    public Category getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getStockQty() {
        return stockQty;
    }

    public boolean isActive() {
        return active;
    }

    public Double getRating() {
        return rating;
    }

    public Long getSalesCount() {
        return salesCount;
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

    public String getPromoLabel() {
        return promoLabel;
    }

    public Instant getPromoStartsAt() {
        return promoStartsAt;
    }

    public Instant getPromoEndsAt() {
        return promoEndsAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setOldPrice(BigDecimal oldPrice) {
        this.oldPrice = oldPrice;
    }

    public void setDiscountPct(Integer discountPct) {
        this.discountPct = discountPct;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setStockQty(Integer stockQty) {
        this.stockQty = stockQty;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public void setSalesCount(Long salesCount) {
        this.salesCount = salesCount;
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

    public void setPromoLabel(String promoLabel) {
        this.promoLabel = promoLabel;
    }

    public void setPromoStartsAt(Instant promoStartsAt) {
        this.promoStartsAt = promoStartsAt;
    }

    public void setPromoEndsAt(Instant promoEndsAt) {
        this.promoEndsAt = promoEndsAt;
    }
}
