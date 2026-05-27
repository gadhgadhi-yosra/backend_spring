package com.elfaddoui.backend.category.repository;

import com.elfaddoui.backend.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByActiveTrueOrderBySortOrderAscNameAsc();
    Optional<Category> findByNameIgnoreCase(String name);
    Optional<Category> findByKeyIgnoreCase(String key);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsByKeyIgnoreCase(String key);
    boolean existsByKeyIgnoreCaseAndIdNot(String key, Long id);
}
