package com.elfaddoui.backend.home.repository;

import com.elfaddoui.backend.home.entity.GroceryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroceryItemRepository extends JpaRepository<GroceryItem, String> {
}
