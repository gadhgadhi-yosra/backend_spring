package com.elfaddoui.backend.category.controller;

import com.elfaddoui.backend.category.dto.PublicCategoryResponse;
import com.elfaddoui.backend.category.dto.PublicCategoryProductsResponse;
import com.elfaddoui.backend.category.service.CategoryService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping(value = "/api/categories", produces = MediaType.APPLICATION_JSON_VALUE)
public class PublicCategoryController {

    private final CategoryService categoryService;

    public PublicCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<PublicCategoryResponse> getCategories() {
        return categoryService.getPublicCategories();
    }

    @GetMapping("/{key}/products")
    public PublicCategoryProductsResponse getCategoryProducts(
            @PathVariable String key,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean promoOnly,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String preset
    ) {
        return categoryService.getPublicCategoryProducts(key, query, promoOnly, minPrice, maxPrice, sort, preset);
    }
}
