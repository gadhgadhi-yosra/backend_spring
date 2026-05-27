package com.elfaddoui.backend.home.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "grocery_items")
public class GroceryItem {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    private String category;

    private Integer quantity;

    private String image;

    public GroceryItem() {
    }

    public GroceryItem(String id, String name, String category, Integer quantity, String image) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
