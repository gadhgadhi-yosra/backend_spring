package com.elfaddoui.backend.product.service;

import com.elfaddoui.backend.product.dto.ProductRequest;
import com.elfaddoui.backend.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
    ProductResponse getById(Long id);
    ProductResponse getAdminById(Long id);
    Page<ProductResponse> getAdminPage(
            String query,
            Long categoryId,
            Boolean active,
            Boolean promoOnly,
            Integer minDiscountPct,
            Integer maxDiscountPct,
            Boolean bioOnly,
            Boolean newOnly,
            Boolean popularOnly,
            Pageable pageable
    );
    Sort resolvePublicSort(String sort);
    Page<ProductResponse> search(String query, Long categoryId, String categoryName, String categoryKey, Boolean promoOnly,
                                 BigDecimal minPrice, BigDecimal maxPrice, String sort, String preset, Pageable pageable);
}
