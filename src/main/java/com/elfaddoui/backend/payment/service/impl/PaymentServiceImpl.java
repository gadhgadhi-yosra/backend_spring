package com.elfaddoui.backend.payment.service.impl;

import com.elfaddoui.backend.admin.service.impl.AdminBackofficeServiceImpl;
import com.elfaddoui.backend.appconfig.service.AppConfigService;
import com.elfaddoui.backend.cart.entity.CartItem;
import com.elfaddoui.backend.cart.repository.CartItemRepository;
import com.elfaddoui.backend.config.AppProperties;
import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.payment.dto.PaymentIntentRequest;
import com.elfaddoui.backend.payment.dto.PaymentIntentResponse;
import com.elfaddoui.backend.payment.service.PaymentService;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final AppConfigService appConfigService;
    private final AppProperties appProperties;

    public PaymentServiceImpl(
            UserRepository userRepository,
            CartItemRepository cartItemRepository,
            AppConfigService appConfigService,
            AppProperties appProperties
    ) {
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.appConfigService = appConfigService;
        this.appProperties = appProperties;
    }

    @Override
    public PaymentIntentResponse createIntent(String userEmail, PaymentIntentRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        BigDecimal serverTotal = calculateCartTotal(user.getId());
        // Never block checkout on a client-provided amount. The server-calculated total is the source of truth.
        // (The mobile app may have a stale total if prices/delivery fee changed since last refresh.)
        validateRequestedTotal(request.amount(), serverTotal);

        String paymentIntentId = "pi_" + UUID.randomUUID().toString().replace("-", "");
        String baseUrl = resolveCheckoutBaseUrl();
        String separator = baseUrl.contains("?") ? "&" : "?";
        String checkoutUrl = baseUrl
                + separator + "paymentIntentId=" + encode(paymentIntentId)
                + "&amount=" + encode(formatMoney(serverTotal))
                + "&currency=" + encode(normalizeCurrency(request.currency()));

        return new PaymentIntentResponse(checkoutUrl, paymentIntentId);
    }

    private String resolveCheckoutBaseUrl() {
        String configured = appProperties.getPayment().getCheckoutBaseUrl();
        if (configured == null) {
            return "http://127.0.0.1:8080/pay";
        }
        String trimmed = configured.trim();
        if (trimmed.isEmpty()) {
            return "http://127.0.0.1:8080/pay";
        }
        // Normalize trailing slash so /pay and /pay/ behave the same for query appending.
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private BigDecimal calculateCartTotal(Long userId) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItemRepository.findByUserIdOrderByUpdatedAtDesc(userId)) {
            Product product = cartItem.getProduct();
            if (product == null || !product.isActive()) {
                throw new IllegalStateException("Cart contains unavailable product");
            }
            if (cartItem.getQuantity() == null || cartItem.getQuantity() <= 0) {
                throw new IllegalStateException("Cart contains invalid quantity");
            }
            if (product.getPrice() == null) {
                throw new IllegalStateException("Product price is missing");
            }
            if (product.getStockQty() == null || product.getStockQty() < cartItem.getQuantity()) {
                throw new IllegalStateException("Requested quantity exceeds available stock");
            }

            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Cart is empty");
        }

        return subtotal.add(resolveDeliveryFee());
    }

    private void validateRequestedTotal(BigDecimal requestedTotal, BigDecimal serverTotal) {
        if (requestedTotal == null) {
            return;
        }
        // Intentionally non-blocking: we still charge based on serverTotal in the checkout URL.
        // If you want stricter behavior, return a 409 with the updated server total instead.
    }

    private BigDecimal resolveDeliveryFee() {
        try {
            return new BigDecimal(appConfigService.getValue(
                    AdminBackofficeServiceImpl.DELIVERY_FEE_KEY,
                    AdminBackofficeServiceImpl.DEFAULT_DELIVERY_FEE.toPlainString()
            ));
        } catch (Exception ignored) {
            return AdminBackofficeServiceImpl.DEFAULT_DELIVERY_FEE;
        }
    }

    private String normalizeCurrency(String currency) {
        return currency == null || currency.isBlank() ? "TND" : currency.trim().toUpperCase();
    }

    private String formatMoney(BigDecimal amount) {
        return money(amount).toPlainString();
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
