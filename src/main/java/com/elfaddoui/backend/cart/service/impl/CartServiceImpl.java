package com.elfaddoui.backend.cart.service.impl;

import com.elfaddoui.backend.cart.dto.AddCartItemRequest;
import com.elfaddoui.backend.cart.dto.CartItemResponse;
import com.elfaddoui.backend.cart.dto.CartResponse;
import com.elfaddoui.backend.cart.dto.UpdateCartItemRequest;
import com.elfaddoui.backend.cart.entity.CartItem;
import com.elfaddoui.backend.cart.repository.CartItemRepository;
import com.elfaddoui.backend.cart.service.CartService;
import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.product.repository.ProductRepository;
import com.elfaddoui.backend.upload.service.PublicImageUrlResolver;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PublicImageUrlResolver publicImageUrlResolver;

    public CartServiceImpl(
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository,
            PublicImageUrlResolver publicImageUrlResolver
    ) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.publicImageUrlResolver = publicImageUrlResolver;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        User user = findUser(userEmail);
        return toResponse(cartItemRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()));
    }

    @Override
    public CartResponse addItem(String userEmail, Long productId, AddCartItemRequest request) {
        User user = findUser(userEmail);
        Product product = findAvailableProduct(productId);

        CartItem item = cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseGet(() -> {
                    CartItem created = new CartItem();
                    created.setUser(user);
                    created.setProduct(product);
                    created.setQuantity(0);
                    return created;
                });

        int nextQty = item.getQuantity() + request.qty();
        validateRequestedQuantity(product, nextQty);
        item.setQuantity(nextQty);
        cartItemRepository.save(item);

        return getCart(userEmail);
    }

    @Override
    public CartResponse updateItemQuantity(String userEmail, Long productId, UpdateCartItemRequest request) {
        User user = findUser(userEmail);
        Product product = findAvailableProduct(productId);
        CartItem item = cartItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        validateRequestedQuantity(product, request.qty());
        item.setQuantity(request.qty());

        return getCart(userEmail);
    }

    @Override
    public void removeItem(String userEmail, Long productId) {
        User user = findUser(userEmail);
        cartItemRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    @Override
    public void clearCart(String userEmail) {
        User user = findUser(userEmail);
        cartItemRepository.deleteByUserId(user.getId());
    }

    private User findUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Product findAvailableProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        if (!product.isActive()) {
            throw new IllegalStateException("Product is inactive");
        }
        return product;
    }

    private void validateRequestedQuantity(Product product, int quantity) {
        if (product.getStockQty() == null || product.getStockQty() <= 0) {
            throw new IllegalStateException("Product is out of stock");
        }
        if (quantity > product.getStockQty()) {
            throw new IllegalStateException("Requested quantity exceeds available stock");
        }
    }

    private CartResponse toResponse(List<CartItem> items) {
        List<CartItemResponse> rows = items.stream()
                .map(this::toItemResponse)
                .toList();

        int totalItems = rows.stream()
                .mapToInt(CartItemResponse::qty)
                .sum();

        BigDecimal subtotal = rows.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(rows, totalItems, subtotal);
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                product.getId(),
                product.getName(),
                publicImageUrlResolver.resolve(product.getImageUrl()),
                product.getPrice(),
                product.getStockQty(),
                item.getQuantity(),
                lineTotal
        );
    }
}
