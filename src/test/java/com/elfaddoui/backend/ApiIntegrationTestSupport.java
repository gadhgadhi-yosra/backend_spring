package com.elfaddoui.backend;

import com.elfaddoui.backend.appconfig.repository.AppConfigRepository;
import com.elfaddoui.backend.cart.repository.CartItemRepository;
import com.elfaddoui.backend.category.entity.Category;
import com.elfaddoui.backend.category.repository.CategoryRepository;
import com.elfaddoui.backend.category.service.CategoryCatalog;
import com.elfaddoui.backend.favorite.repository.FavoriteRepository;
import com.elfaddoui.backend.loyalty.repository.LoyaltyAccountRepository;
import com.elfaddoui.backend.loyalty.repository.LoyaltyGiftRepository;
import com.elfaddoui.backend.loyalty.repository.LoyaltyLedgerEntryRepository;
import com.elfaddoui.backend.loyalty.repository.LoyaltyVoucherRepository;
import com.elfaddoui.backend.order.repository.OrderRepository;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.product.repository.ProductRepository;
import com.elfaddoui.backend.security.JwtService;
import com.elfaddoui.backend.user.entity.Role;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ApiIntegrationTestSupport {

    private final CategoryCatalog categoryCatalog = new CategoryCatalog();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected FavoriteRepository favoriteRepository;

    @Autowired
    protected CartItemRepository cartItemRepository;

    @Autowired
    protected OrderRepository orderRepository;

    @Autowired
    protected LoyaltyLedgerEntryRepository loyaltyLedgerEntryRepository;

    @Autowired
    protected LoyaltyVoucherRepository loyaltyVoucherRepository;

    @Autowired
    protected LoyaltyAccountRepository loyaltyAccountRepository;

    @Autowired
    protected LoyaltyGiftRepository loyaltyGiftRepository;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected AppConfigRepository appConfigRepository;

    @BeforeEach
    void cleanDatabase() {
        cartItemRepository.deleteAll();
        favoriteRepository.deleteAll();
        loyaltyLedgerEntryRepository.deleteAll();
        loyaltyVoucherRepository.deleteAll();
        loyaltyAccountRepository.deleteAll();
        loyaltyGiftRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        appConfigRepository.deleteAll();
    }

    protected String adminToken() {
        return tokenFor(Role.ADMIN, "admin@elfaddoui.test");
    }

    protected String clientToken() {
        return tokenFor(Role.CLIENT, "client@elfaddoui.test");
    }

    protected String clientToken(String email) {
        return tokenFor(Role.CLIENT, email);
    }

    protected Category createCategory(String name) {
        return createCategory(name, categoryCatalog.keyFor(name), categoryCatalog.displayNameFor(name), 0, false, false, false, false);
    }

    protected Category createCategory(
            String name,
            String key,
            String displayName,
            int sortOrder
    ) {
        return createCategory(name, key, displayName, sortOrder, false, false, false, false);
    }

    protected Category createCategory(
            String name,
            String key,
            String displayName,
            int sortOrder,
            boolean promo,
            boolean bio,
            boolean isNew,
            boolean popular
    ) {
        Category category = new Category(name, true);
        category.setKey(categoryCatalog.keyFor(key));
        category.setDisplayName(displayName);
        category.setSortOrder(sortOrder);
        category.setPromo(promo);
        category.setBio(bio);
        category.setNew(isNew);
        category.setPopular(popular);
        return categoryRepository.save(category);
    }

    protected Product createProduct(
            String name,
            Category category,
            BigDecimal price,
            int discountPct,
            double rating,
            boolean active
    ) {
        return createProduct(name, category, price, discountPct, rating, active, discountPct > 0, false, false, false);
    }

    protected Product createProduct(
            String name,
            Category category,
            BigDecimal price,
            int discountPct,
            double rating,
            boolean active,
            boolean promo,
            boolean bio,
            boolean isNew,
            boolean popular
    ) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(name + " description");
        product.setPrice(price);
        product.setOldPrice(discountPct > 0 ? price.add(BigDecimal.ONE) : null);
        product.setDiscountPct(discountPct);
        product.setCategory(category);
        product.setImageUrl("https://cdn.example.com/" + name.replace(" ", "-").toLowerCase() + ".jpg");
        product.setStockQty(20);
        product.setRating(rating);
        product.setActive(active);
        product.setPromo(promo);
        product.setBio(bio);
        product.setNew(isNew);
        product.setPopular(popular);
        product.setSalesCount(0L);
        return productRepository.save(product);
    }

    private String tokenFor(Role role, String email) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User created = new User("Test " + role.name(), email, passwordEncoder.encode("password123"));
            created.setRoles(Set.of(role));
            return userRepository.save(created);
        });
        return jwtService.generate(user.getEmail());
    }
}
