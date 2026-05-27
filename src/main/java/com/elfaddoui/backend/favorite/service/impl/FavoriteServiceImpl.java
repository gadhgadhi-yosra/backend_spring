package com.elfaddoui.backend.favorite.service.impl;

import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.favorite.dto.FavoriteResponse;
import com.elfaddoui.backend.favorite.dto.FavoriteToggleResponse;
import com.elfaddoui.backend.favorite.entity.Favorite;
import com.elfaddoui.backend.favorite.repository.FavoriteRepository;
import com.elfaddoui.backend.favorite.service.FavoriteService;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.product.repository.ProductRepository;
import com.elfaddoui.backend.upload.service.PublicImageUrlResolver;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PublicImageUrlResolver publicImageUrlResolver;

    public FavoriteServiceImpl(
            FavoriteRepository favoriteRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            PublicImageUrlResolver publicImageUrlResolver
    ) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.publicImageUrlResolver = publicImageUrlResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavorites(String userEmail) {
        User user = findUser(userEmail);
        return favoriteRepository.findByUserIdAndProductActiveTrueOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public FavoriteResponse add(String userEmail, Long productId) {
        User user = findUser(userEmail);
        Product product = findProduct(productId);

        return favoriteRepository.findByUserIdAndProductId(user.getId(), productId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    Favorite favorite = new Favorite();
                    favorite.setUser(user);
                    favorite.setProduct(product);
                    return toResponse(favoriteRepository.save(favorite));
                });
    }

    @Override
    public void remove(String userEmail, Long productId) {
        User user = findUser(userEmail);
        favoriteRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    @Override
    public FavoriteToggleResponse toggle(String userEmail, Long productId) {
        User user = findUser(userEmail);
        Product product = findProduct(productId);

        return favoriteRepository.findByUserIdAndProductId(user.getId(), productId)
                .map(existing -> {
                    favoriteRepository.delete(existing);
                    return new FavoriteToggleResponse(false, null);
                })
                .orElseGet(() -> {
                    Favorite favorite = new Favorite();
                    favorite.setUser(user);
                    favorite.setProduct(product);
                    Favorite saved = favoriteRepository.save(favorite);
                    return new FavoriteToggleResponse(true, toResponse(saved));
                });
    }

    private User findUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Product findProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (!product.isActive()) {
            throw new IllegalStateException("Product is inactive");
        }
        return product;
    }

    private FavoriteResponse toResponse(Favorite favorite) {
        Product product = favorite.getProduct();
        return new FavoriteResponse(
                favorite.getId(),
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getOldPrice(),
                product.getDiscountPct(),
                publicImageUrlResolver.resolve(product.getImageUrl()),
                product.getRating(),
                product.getSalesCount(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                favorite.getCreatedAt()
        );
    }
}
