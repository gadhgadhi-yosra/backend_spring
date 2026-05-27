package com.elfaddoui.backend.home.service.impl;

import com.elfaddoui.backend.appconfig.service.AppConfigService;
import com.elfaddoui.backend.category.entity.Category;
import com.elfaddoui.backend.category.repository.CategoryRepository;
import com.elfaddoui.backend.config.AppProperties;
import com.elfaddoui.backend.home.dto.HomeResponse;
import com.elfaddoui.backend.home.dto.HomeResponse.HomeCatalogueDto;
import com.elfaddoui.backend.home.dto.HomeResponse.HomePromotionDto;
import com.elfaddoui.backend.home.mapper.HomeProductMapper;
import com.elfaddoui.backend.home.service.HomeService;
import com.elfaddoui.backend.home.util.DeliveryAreaNormalizer;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.product.repository.ProductRepository;
import com.elfaddoui.backend.upload.service.PublicImageUrlResolver;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HomeServiceImpl implements HomeService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final HomeProductMapper homeProductMapper;
    private final AppConfigService appConfigService;
    private final AppProperties appProperties;
    private final PublicImageUrlResolver publicImageUrlResolver;

    public HomeServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            HomeProductMapper homeProductMapper,
            AppConfigService appConfigService,
            AppProperties appProperties,
            PublicImageUrlResolver publicImageUrlResolver
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.homeProductMapper = homeProductMapper;
        this.appConfigService = appConfigService;
        this.appProperties = appProperties;
        this.publicImageUrlResolver = publicImageUrlResolver;
    }

    @Override
    public HomeResponse getHome() {
        Instant now = Instant.now();
        List<Product> deals = productRepository.findActiveHomeDeals(now, PageRequest.of(0, 6));
        if (deals.isEmpty()) {
            deals = productRepository.findTop6ByActiveTrueAndDiscountPctGreaterThanOrderByDiscountPctDescUpdatedAtDesc(0);
        }
        List<Product> forYou = productRepository.findTop8ByActiveTrueOrderByRatingDescSalesCountDescUpdatedAtDesc();
        List<Product> recent = productRepository.findTop6ByActiveTrueOrderByCreatedAtDesc();
        List<Category> categories = categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc();

        return new HomeResponse(
                appConfigService.getValue("home.locationLabel", appProperties.getHome().getLocationLabel()),
                appConfigService.getValue("home.etaLabel", appProperties.getHome().getEtaLabel()),
                deals.stream().map(homeProductMapper::toDto).toList(),
                forYou.stream().map(homeProductMapper::toDto).toList(),
                recent.stream().map(homeProductMapper::toDto).toList(),
                deliveryAreas(),
                categories.stream().limit(8).map(this::toCatalogueDto).toList(),
                deals.stream().map(this::toPromotionDto).toList()
        );
    }

    private List<String> deliveryAreas() {
        String raw = appConfigService.getValue("home.deliveryAreas", "Ghardimaoui,Weghech,Kalaa");
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(DeliveryAreaNormalizer::canonicalize)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private HomeCatalogueDto toCatalogueDto(Category category) {
        return new HomeCatalogueDto(
                category.getId(),
                category.getKey(),
                category.getDisplayName(),
                publicImageUrlResolver.resolve(category.getImageUrl()),
                category.getSortOrder() == null ? 0 : category.getSortOrder()
        );
    }

    private HomePromotionDto toPromotionDto(Product product) {
        String label = product.getPromoLabel();
        String title = label == null || label.isBlank() ? product.getName() : label;
        String subtitle = product.getDiscountPct() == null || product.getDiscountPct() <= 0
                ? product.getDescription()
                : "Jusqu'a -" + product.getDiscountPct() + "%";
        return new HomePromotionDto(
                String.valueOf(product.getId()),
                title,
                subtitle,
                publicImageUrlResolver.resolve(product.getImageUrl()),
                product.getDiscountPct(),
                product.getPromoStartsAt(),
                product.getPromoEndsAt(),
                product.getId(),
                product.getName()
        );
    }
}
