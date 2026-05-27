package com.elfaddoui.backend.category.service.impl;

import com.elfaddoui.backend.category.dto.CategoryRequest;
import com.elfaddoui.backend.category.dto.PublicFilterOptionResponse;
import com.elfaddoui.backend.category.dto.CategoryResponse;
import com.elfaddoui.backend.category.dto.PublicCategoryProductsResponse;
import com.elfaddoui.backend.category.dto.PublicCategoryResponse;
import com.elfaddoui.backend.category.entity.Category;
import com.elfaddoui.backend.category.repository.CategoryRepository;
import com.elfaddoui.backend.category.service.CategoryCatalog;
import com.elfaddoui.backend.category.service.CategoryService;
import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.product.dto.ProductResponse;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.product.mapper.PublicProductMapper;
import com.elfaddoui.backend.product.repository.ProductRepository;
import com.elfaddoui.backend.product.service.ProductService;
import com.elfaddoui.backend.product.service.impl.PublicProductPreset;
import com.elfaddoui.backend.product.service.impl.ProductSpecifications;
import com.elfaddoui.backend.upload.service.PublicImageUrlResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryCatalog categoryCatalog;
    private final PublicProductMapper publicProductMapper;
    private final ProductService productService;
    private final PublicImageUrlResolver publicImageUrlResolver;

    public CategoryServiceImpl(
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            CategoryCatalog categoryCatalog,
            PublicProductMapper publicProductMapper,
            ProductService productService,
            PublicImageUrlResolver publicImageUrlResolver
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryCatalog = categoryCatalog;
        this.publicProductMapper = publicProductMapper;
        this.productService = productService;
        this.publicImageUrlResolver = publicImageUrlResolver;
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalStateException("Category name already exists");
        }
        String resolvedKey = resolveKey(request.name(), request.key());
        if (categoryRepository.existsByKeyIgnoreCase(resolvedKey)) {
            throw new IllegalStateException("Category key already exists");
        }
        Category category = new Category(request.name().trim(), request.isActive());
        applyPresentation(category, request, resolvedKey);
        Category saved = categoryRepository.save(category);
        return toResponse(saved, computeAdminStats(saved));
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw new IllegalStateException("Category name already exists");
        }
        String resolvedKey = resolveKey(request.name(), request.key());
        if (categoryRepository.existsByKeyIgnoreCaseAndIdNot(resolvedKey, id)) {
            throw new IllegalStateException("Category key already exists");
        }
        Category category = findEntity(id);
        category.setName(request.name().trim());
        category.setActive(request.isActive());
        applyPresentation(category, request, resolvedKey);
        return toResponse(category, computeAdminStats(category));
    }

    @Override
    public void delete(Long id) {
        Category category = findEntity(id);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = findEntity(id);
        return toResponse(category, computeAdminStats(category));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAdminPage(Pageable pageable) {
        Page<Category> page = categoryRepository.findAll(pageable);
        List<Long> categoryIds = page.getContent().stream()
                .map(Category::getId)
                .toList();
        Map<Long, CategoryAdminStats> statsByCategoryId = computeAdminStatsByCategoryIds(categoryIds);
        return page.map(category -> toResponse(category, statsByCategoryId.getOrDefault(category.getId(), CategoryAdminStats.empty())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(category -> toResponse(category, computeAdminStats(category)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublicCategoryResponse> getPublicCategories() {
        List<Category> categories = categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc();
        List<Product> products = productRepository.findAll(
                ProductSpecifications.activeOnly(),
                Sort.by(Sort.Order.asc("category.sortOrder"), Sort.Order.asc("category.name"), Sort.Order.asc("name"))
        );
        Map<String, CategoryAggregate> aggregatesByKey = new LinkedHashMap<>();
        for (Category category : categories) {
            String key = categoryKey(category);
            aggregatesByKey.computeIfAbsent(key, ignored -> new CategoryAggregate(key))
                    .categories()
                    .add(category);
        }
        for (Product product : products) {
            String key = categoryKey(product.getCategory());
            aggregatesByKey.computeIfAbsent(key, ignored -> new CategoryAggregate(key))
                    .products()
                    .add(product);
        }

        return aggregatesByKey.values().stream()
                .map(this::toPublicResponse)
                .sorted(Comparator
                        .comparingInt(PublicCategoryResponse::sortOrder)
                        .thenComparing(PublicCategoryResponse::name))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PublicCategoryProductsResponse getPublicCategoryProducts(
            String categoryKey,
            String query,
            Boolean promoOnly,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sort,
            String preset
    ) {
        String normalizedKey = categoryCatalog.keyFor(categoryKey);
        Category category = categoryRepository.findByKeyIgnoreCase(normalizedKey)
                .filter(Category::isActive)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        String resolvedPreset = PublicProductPreset.normalize(preset);
        Boolean resolvedPromoOnly = PublicProductPreset.resolvePromoOnly(resolvedPreset, promoOnly);
        String resolvedSort = PublicProductPreset.resolveSort(resolvedPreset, sort);

        List<Product> products = productRepository.findAll(
                org.springframework.data.jpa.domain.Specification.where(ProductSpecifications.activeOnly())
                        .and(ProductSpecifications.hasCategory(category.getId()))
                        .and(ProductSpecifications.nameOrDescriptionContains(query))
                        .and(ProductSpecifications.promoOnly(resolvedPromoOnly))
                        .and(ProductSpecifications.minPrice(minPrice))
                        .and(ProductSpecifications.maxPrice(maxPrice))
                        .and(PublicProductPreset.resolveSpecification(resolvedPreset)),
                productService.resolvePublicSort(resolvedSort)
        );

        CategoryAggregate aggregate = new CategoryAggregate(normalizedKey);
        aggregate.categories().add(category);
        aggregate.products().addAll(products);

        PublicCategoryResponse summary = toPublicResponse(aggregate);
        List<ProductResponse> productResponses = products.stream()
                .map(publicProductMapper::toResponse)
                .toList();
        List<String> subCategories = List.of(category.getName());

        return new PublicCategoryProductsResponse(
                summary.id(),
                summary.key(),
                summary.name(),
                summary.productCount(),
                summary.promoCount(),
                summary.imageUrl(),
                summary.reviews(),
                summary.tags(),
                buildTabs(productResponses, resolvedPreset, resolvedSort),
                buildChips(productResponses, resolvedPreset, resolvedSort, resolvedPromoOnly),
                subCategories,
                productResponses
        );
    }

    private Category findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    private CategoryResponse toResponse(Category category, CategoryAdminStats stats) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                categoryKey(category),
                categoryDisplayName(category),
                categoryImage(category),
                categorySortOrder(category),
                category.isActive(),
                category.isPromo(),
                category.isBio(),
                category.isNew(),
                category.isPopular(),
                splitTags(category.getCustomTags()),
                stats.productCount(),
                stats.activeProductCount(),
                stats.inactiveProductCount(),
                stats.promoProductCount(),
                stats.bioProductCount(),
                stats.newProductCount(),
                stats.popularProductCount(),
                stats.totalStockQty(),
                stats.maxDiscountPct(),
                stats.averageRating(),
                stats.minPrice(),
                stats.maxPrice(),
                stats.tags()
        );
    }

    private PublicCategoryResponse toPublicResponse(CategoryAggregate aggregate) {
        List<Product> products = aggregate.products();
        List<ProductResponse> productResponses = productResponses(products);
        int promoCount = (int) productResponses.stream().filter(ProductResponse::isPromo).count();
        int reviews = products.isEmpty()
                ? 0
                : (int) products.stream().mapToInt(this::estimateReviews).average().orElse(0);
        String key = aggregate.key();
        Category representative = aggregate.categories().isEmpty() ? null : aggregate.categories().getFirst();
        boolean featuredPromo = representative != null && representative.isPromo();
        boolean featuredBio = representative != null && representative.isBio();
        boolean featuredNew = representative != null && representative.isNew();
        boolean featuredPopular = representative != null && representative.isPopular();
        List<String> tags = buildTags(key, promoCount, reviews, featuredPromo, featuredBio, featuredNew, featuredPopular, products.size());
        String fallbackImage = products.isEmpty() ? null : publicImageUrlResolver.resolve(products.getFirst().getImageUrl());

        return new PublicCategoryResponse(
                representative == null ? null : representative.getId(),
                key,
                representative == null ? categoryCatalog.displayNameFor(key) : categoryDisplayName(representative),
                true,
                featuredPromo,
                featuredBio,
                featuredNew,
                featuredPopular,
                representative == null ? List.of() : splitTags(representative.getCustomTags()),
                products.size(),
                promoCount,
                representative == null ? fallbackImage : categoryImage(representative, fallbackImage),
                reviews,
                tags,
                buildTabs(productResponses, null, null),
                buildChips(productResponses, null, null, null),
                representative == null ? 0 : categorySortOrder(representative)
        );
    }

    private List<PublicFilterOptionResponse> buildTabs(
            List<ProductResponse> products,
            String preset,
            String sort
    ) {
        int productCount = products.size();
        int topCount = (int) products.stream().filter(ProductResponse::isPopular).count();
        int newCount = (int) products.stream().filter(ProductResponse::isNew).count();
        int classicCount = Math.max(0, productCount - newCount);
        return List.of(
                new PublicFilterOptionResponse("all", "Tout", "preset", PublicProductPreset.ALL, productCount,
                        preset == null || PublicProductPreset.ALL.equals(preset)),
                new PublicFilterOptionResponse("top", "Top", "preset", PublicProductPreset.TOP, topCount,
                        PublicProductPreset.TOP.equals(preset) || "top".equals(sort)),
                new PublicFilterOptionResponse("classic", "Classiques", "preset", PublicProductPreset.CLASSIC, classicCount,
                        PublicProductPreset.CLASSIC.equals(preset)),
                new PublicFilterOptionResponse("new", "Nouveaux", "preset", PublicProductPreset.NEW, newCount,
                        PublicProductPreset.NEW.equals(preset) || "newest".equals(sort))
        );
    }

    private List<PublicFilterOptionResponse> buildChips(
            List<ProductResponse> products,
            String preset,
            String sort,
            Boolean promoOnly
    ) {
        int productCount = products.size();
        int promoCount = (int) products.stream().filter(ProductResponse::isPromo).count();
        int recommendedCount = (int) products.stream().filter(product -> product.isPopular() || product.isPromo()).count();
        return List.of(
                new PublicFilterOptionResponse("products", productCount + " produits", "preset", PublicProductPreset.ALL, productCount,
                        preset == null || PublicProductPreset.ALL.equals(preset)),
                new PublicFilterOptionResponse("promo", promoCount + " promos", "preset", PublicProductPreset.PROMO, promoCount,
                        PublicProductPreset.PROMO.equals(preset) || Boolean.TRUE.equals(promoOnly)),
                new PublicFilterOptionResponse("recommended", "Recommandé", "preset", PublicProductPreset.RECOMMENDED, recommendedCount,
                        PublicProductPreset.RECOMMENDED.equals(preset) || "recommended".equals(sort))
        );
    }

    private List<ProductResponse> productResponses(List<Product> products) {
        return products.stream()
                .map(publicProductMapper::toResponse)
                .toList();
    }

    private List<String> buildTags(
            String key,
            int promoCount,
            int reviews,
            boolean featuredPromo,
            boolean featuredBio,
            boolean featuredNew,
            boolean featuredPopular,
            int productCount
    ) {
        List<String> tags = new ArrayList<>();
        if (featuredPromo) {
            tags.add("Promos");
        }
        if (featuredPopular) {
            tags.add("Populaires");
        }
        if (featuredBio) {
            tags.add("Bio");
        }
        if (featuredNew) {
            tags.add("Nouveaux");
        }
        if (tags.isEmpty()) {
            tags.add("Categorie");
        }
        return List.copyOf(tags);
    }

    private int estimateReviews(Product product) {
        int fromSales = product.getSalesCount() == null ? 0 : Math.toIntExact(product.getSalesCount());
        int fromRating = product.getRating() == null ? 0 : (int) Math.round(product.getRating() * 30);
        return Math.max(fromSales, fromRating);
    }

    private Map<Long, CategoryAdminStats> computeAdminStatsByCategoryIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        List<Product> products = productRepository.findAll(ProductSpecifications.hasCategoryIds(categoryIds));
        Map<Long, List<Product>> productsByCategoryId = new HashMap<>();
        for (Product product : products) {
            productsByCategoryId.computeIfAbsent(product.getCategory().getId(), ignored -> new ArrayList<>())
                    .add(product);
        }

        Map<Long, CategoryAdminStats> statsByCategoryId = new HashMap<>();
        for (Long categoryId : categoryIds) {
            statsByCategoryId.put(categoryId, buildAdminStats(productsByCategoryId.getOrDefault(categoryId, List.of())));
        }
        return statsByCategoryId;
    }

    private CategoryAdminStats computeAdminStats(Category category) {
        List<Product> products = productRepository.findAll(ProductSpecifications.hasCategory(category.getId()));
        return buildAdminStats(products);
    }

    private CategoryAdminStats buildAdminStats(List<Product> products) {
        if (products.isEmpty()) {
            return CategoryAdminStats.empty();
        }

        List<ProductResponse> responses = productResponses(products);
        int activeProductCount = (int) products.stream().filter(Product::isActive).count();
        int inactiveProductCount = products.size() - activeProductCount;
        int promoProductCount = (int) responses.stream().filter(ProductResponse::isPromo).count();
        int bioProductCount = (int) responses.stream().filter(ProductResponse::isBio).count();
        int newProductCount = (int) responses.stream().filter(ProductResponse::isNew).count();
        int popularProductCount = (int) responses.stream().filter(ProductResponse::isPopular).count();
        int totalStockQty = products.stream().map(Product::getStockQty).filter(stock -> stock != null).mapToInt(Integer::intValue).sum();
        Integer maxDiscountPct = products.stream()
                .map(Product::getDiscountPct)
                .filter(discount -> discount != null)
                .max(Integer::compareTo)
                .orElse(0);
        Double averageRating = products.stream()
                .map(Product::getRating)
                .filter(rating -> rating != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        BigDecimal minPrice = products.stream()
                .map(Product::getPrice)
                .filter(price -> price != null)
                .min(BigDecimal::compareTo)
                .orElse(null);
        BigDecimal maxPrice = products.stream()
                .map(Product::getPrice)
                .filter(price -> price != null)
                .max(BigDecimal::compareTo)
                .orElse(null);

        List<String> tags = new ArrayList<>();
        if (promoProductCount > 0) {
            tags.add("promo");
        }
        if (bioProductCount > 0) {
            tags.add("bio");
        }
        if (newProductCount > 0) {
            tags.add("new");
        }
        if (popularProductCount > 0) {
            tags.add("popular");
        }

        return new CategoryAdminStats(
                products.size(),
                activeProductCount,
                inactiveProductCount,
                promoProductCount,
                bioProductCount,
                newProductCount,
                popularProductCount,
                totalStockQty,
                maxDiscountPct,
                averageRating,
                minPrice,
                maxPrice,
                List.copyOf(tags)
        );
    }

    private void applyPresentation(Category category, CategoryRequest request, String resolvedKey) {
        category.setKey(resolvedKey);
        category.setDisplayName(resolveDisplayName(request.name(), request.displayName()));
        category.setImageUrl(blankToNull(request.imageUrl()));
        category.setSortOrder(resolveSortOrder(request.sortOrder()));
        category.setPromo(request.isPromo());
        category.setBio(request.isBio());
        category.setNew(request.isNew());
        category.setPopular(request.isPopular());
        category.setCustomTags(joinTags(request.customTags()));
    }

    private String resolveKey(String name, String requestedKey) {
        String source = requestedKey == null || requestedKey.isBlank() ? name : requestedKey;
        String resolved = categoryCatalog.keyFor(source);
        if (resolved.isBlank()) {
            throw new IllegalArgumentException("Category key must not be blank");
        }
        return resolved;
    }

    private String resolveDisplayName(String name, String displayName) {
        return (displayName == null || displayName.isBlank())
                ? categoryCatalog.displayNameFor(name)
                : displayName.trim();
    }

    private Integer resolveSortOrder(Integer sortOrder) {
        return sortOrder == null ? 0 : sortOrder;
    }

    private String categoryKey(Category category) {
        String key = category.getKey();
        return (key == null || key.isBlank()) ? categoryCatalog.keyFor(category.getName()) : categoryCatalog.keyFor(key);
    }

    private String categoryDisplayName(Category category) {
        String displayName = category.getDisplayName();
        return (displayName == null || displayName.isBlank()) ? categoryCatalog.displayNameFor(category.getName()) : displayName;
    }

    private String categoryImage(Category category) {
        return categoryImage(category, null);
    }

    private String categoryImage(Category category, String fallback) {
        String imageUrl = category.getImageUrl();
        if (imageUrl != null && !imageUrl.isBlank()) {
            return publicImageUrlResolver.resolve(imageUrl);
        }
        return fallback;
    }

    private int categorySortOrder(Category category) {
        return category.getSortOrder() == null ? 0 : category.getSortOrder();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String joinTags(List<String> tags) {
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

    private List<String> splitTags(String rawTags) {
        if (rawTags == null || rawTags.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(rawTags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .toList();
    }

    private record CategoryAggregate(String key, List<Category> categories, List<Product> products) {
        private CategoryAggregate(String key) {
            this(key, new ArrayList<>(), new ArrayList<>());
        }
    }
}
