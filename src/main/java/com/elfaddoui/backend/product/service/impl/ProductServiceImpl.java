package com.elfaddoui.backend.product.service.impl;

import com.elfaddoui.backend.category.entity.Category;
import com.elfaddoui.backend.category.repository.CategoryRepository;
import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.product.dto.ProductRequest;
import com.elfaddoui.backend.product.dto.ProductResponse;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.product.mapper.PublicProductMapper;
import com.elfaddoui.backend.product.repository.ProductRepository;
import com.elfaddoui.backend.product.service.ProductService;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PublicProductMapper publicProductMapper;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            PublicProductMapper publicProductMapper
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.publicProductMapper = publicProductMapper;
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntity(id);
        apply(product, request);
        return toResponse(product);
    }

    @Override
    public void delete(Long id) {
        Product product = findEntity(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return publicProductMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getAdminById(Long id) {
        return toResponse(findEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAdminPage(
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
    ) {
        Specification<Product> specification = Specification.where(ProductSpecifications.nameOrDescriptionContains(query))
                .and(ProductSpecifications.hasCategory(categoryId))
                .and(ProductSpecifications.activeEquals(active))
                .and(ProductSpecifications.promoOnly(promoOnly))
                .and(ProductSpecifications.minDiscountPct(minDiscountPct))
                .and(ProductSpecifications.maxDiscountPct(maxDiscountPct))
                .and(ProductSpecifications.bioOnly(bioOnly))
                .and(ProductSpecifications.isNew(newOnly))
                .and(ProductSpecifications.popularOnly(popularOnly));

        Pageable resolvedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        return productRepository.findAll(specification, resolvedPageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String query, Long categoryId, String categoryName, String categoryKey, Boolean promoOnly,
                                        BigDecimal minPrice, BigDecimal maxPrice, String sort, String preset, Pageable pageable) {
        String resolvedPreset = PublicProductPreset.normalize(preset);
        Boolean resolvedPromoOnly = PublicProductPreset.resolvePromoOnly(resolvedPreset, promoOnly);
        String resolvedSort = PublicProductPreset.resolveSort(resolvedPreset, sort);

        Specification<Product> specification = Specification.where(ProductSpecifications.activeOnly())
                .and(ProductSpecifications.nameOrDescriptionContains(query))
                .and(ProductSpecifications.hasCategory(categoryId))
                .and(ProductSpecifications.hasCategoryName(categoryName))
                .and(ProductSpecifications.hasCategoryKey(categoryKey))
                .and(ProductSpecifications.promoOnly(resolvedPromoOnly))
                .and(ProductSpecifications.minPrice(minPrice))
                .and(ProductSpecifications.maxPrice(maxPrice))
                .and(PublicProductPreset.resolveSpecification(resolvedPreset));

        Pageable resolvedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                resolvePublicSort(resolvedSort, pageable.getSort())
        );

        return productRepository.findAll(specification, resolvedPageable).map(publicProductMapper::toResponse);
    }

    private void apply(Product product, ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        product.setName(request.name().trim());
        product.setDescription(request.description().trim());
        product.setPrice(request.price());
        if (!request.isPromo()) {
            product.setDiscountPct(0);
            product.setOldPrice(null);
        } else {
            if (request.discountPct() == null || request.discountPct() <= 0) {
                throw new IllegalArgumentException("discountPct must be > 0 when isPromo=true");
            }
            if (request.oldPrice() == null || request.oldPrice().compareTo(request.price()) < 0) {
                throw new IllegalArgumentException("oldPrice must be >= price when isPromo=true");
            }
            if (request.promoStartsAt() != null && request.promoEndsAt() != null
                    && request.promoEndsAt().isBefore(request.promoStartsAt())) {
                throw new IllegalArgumentException("promoEndsAt must be after promoStartsAt");
            }
            product.setDiscountPct(request.discountPct());
            product.setOldPrice(request.oldPrice());
        }
        product.setCategory(category);
        product.setImageUrl(request.imageUrl().trim());
        product.setStockQty(request.stockQty());
        product.setActive(request.isActive());
        product.setPromo(request.isPromo());
        product.setBio(request.isBio());
        product.setNew(request.isNew());
        product.setPopular(request.isPopular());
        product.setCustomTags(joinTags(request.customTags()));
        product.setPromoLabel(blankToNull(request.promoLabel()));
        product.setPromoStartsAt(request.isPromo() ? request.promoStartsAt() : null);
        product.setPromoEndsAt(request.isPromo() ? request.promoEndsAt() : null);
        product.setRating(request.rating() == null ? 0.0 : request.rating());
        product.setSalesCount(request.salesCount() == null ? 0L : request.salesCount());
    }

    private Product findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    @Override
    public Sort resolvePublicSort(String sort) {
        return resolvePublicSort(sort, Sort.unsorted());
    }

    private Sort resolvePublicSort(String sort, Sort fallback) {
        if (sort == null || sort.isBlank()) {
            return fallback.isSorted() ? fallback : Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return switch (sort) {
            case "recommended" -> Sort.by(Sort.Direction.DESC, "rating")
                    .and(Sort.by(Sort.Direction.DESC, "discountPct"))
                    .and(Sort.by(Sort.Direction.DESC, "salesCount"))
                    .and(Sort.by(Sort.Direction.DESC, "updatedAt"));
            case "top" -> Sort.by(Sort.Direction.DESC, "salesCount")
                    .and(Sort.by(Sort.Direction.DESC, "rating"))
                    .and(Sort.by(Sort.Direction.DESC, "updatedAt"));
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "priceLow" -> Sort.by(Sort.Direction.ASC, "price");
            case "priceHigh" -> Sort.by(Sort.Direction.DESC, "price");
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "price");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "price");
            case "recent" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "salesCount"));
            case "discount" -> Sort.by(Sort.Direction.DESC, "discountPct").and(Sort.by(Sort.Direction.DESC, "updatedAt"));
            case "name" -> Sort.by(Sort.Direction.ASC, "name");
            default -> throw new IllegalArgumentException("Unsupported sort value");
        };
    }

    public ProductResponse toResponse(Product product) {
        return publicProductMapper.toResponse(product);
    }

    private String joinTags(java.util.List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.stream()
                .map(tag -> tag == null ? "" : tag.trim())
                .filter(tag -> !tag.isBlank())
                .distinct()
                .reduce((left, right) -> left + "," + right)
                .orElse(null);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
