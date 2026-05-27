package com.elfaddoui.backend.category.service;

import com.elfaddoui.backend.category.dto.CategoryRequest;
import com.elfaddoui.backend.category.dto.CategoryResponse;
import com.elfaddoui.backend.category.dto.PublicCategoryResponse;
import com.elfaddoui.backend.category.dto.PublicCategoryProductsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    CategoryResponse update(Long id, CategoryRequest request);
    void delete(Long id);
    CategoryResponse getById(Long id);
    Page<CategoryResponse> getAdminPage(Pageable pageable);
    List<CategoryResponse> getActiveCategories();
    List<PublicCategoryResponse> getPublicCategories();
    PublicCategoryProductsResponse getPublicCategoryProducts(
            String categoryKey,
            String query,
            Boolean promoOnly,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            String preset
    );
}
