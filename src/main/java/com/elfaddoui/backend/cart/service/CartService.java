package com.elfaddoui.backend.cart.service;

import com.elfaddoui.backend.cart.dto.AddCartItemRequest;
import com.elfaddoui.backend.cart.dto.CartResponse;
import com.elfaddoui.backend.cart.dto.UpdateCartItemRequest;

public interface CartService {
    CartResponse getCart(String userEmail);
    CartResponse addItem(String userEmail, Long productId, AddCartItemRequest request);
    CartResponse updateItemQuantity(String userEmail, Long productId, UpdateCartItemRequest request);
    void removeItem(String userEmail, Long productId);
    void clearCart(String userEmail);
}
