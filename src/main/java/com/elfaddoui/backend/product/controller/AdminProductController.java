package com.elfaddoui.backend.product.controller;

import com.elfaddoui.backend.product.dto.ProductRequest;
import com.elfaddoui.backend.product.dto.ProductResponse;
import com.elfaddoui.backend.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/admin/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<ProductResponse> getPage(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean promoOnly,
            @RequestParam(required = false) Integer minDiscountPct,
            @RequestParam(required = false) Integer maxDiscountPct,
            @RequestParam(required = false) Boolean bioOnly,
            @RequestParam(required = false) Boolean newOnly,
            @RequestParam(required = false) Boolean popularOnly,
            Pageable pageable
    ) {
        return productService.getAdminPage(
                query,
                categoryId,
                active,
                promoOnly,
                minDiscountPct,
                maxDiscountPct,
                bioOnly,
                newOnly,
                popularOnly,
                pageable
        );
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getAdminById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
