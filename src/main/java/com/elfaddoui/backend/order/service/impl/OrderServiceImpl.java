package com.elfaddoui.backend.order.service.impl;

import com.elfaddoui.backend.admin.service.impl.AdminBackofficeServiceImpl;
import com.elfaddoui.backend.appconfig.service.AppConfigService;
import com.elfaddoui.backend.cart.entity.CartItem;
import com.elfaddoui.backend.cart.repository.CartItemRepository;
import com.elfaddoui.backend.exception.NotFoundException;
import com.elfaddoui.backend.notification.service.NotificationService;
import com.elfaddoui.backend.order.dto.CheckoutRequest;
import com.elfaddoui.backend.order.dto.CheckoutResponse;
import com.elfaddoui.backend.order.dto.DeliverySettingsResponse;
import com.elfaddoui.backend.order.dto.OrderDetailsItemResponse;
import com.elfaddoui.backend.order.dto.OrderDetailsResponse;
import com.elfaddoui.backend.order.dto.DeliveryTrackingResponse;
import com.elfaddoui.backend.order.dto.OrderHistoryItemResponse;
import com.elfaddoui.backend.order.entity.DeliverySlot;
import com.elfaddoui.backend.order.entity.Order;
import com.elfaddoui.backend.order.entity.OrderItem;
import com.elfaddoui.backend.order.entity.OrderStatus;
import com.elfaddoui.backend.order.entity.PaymentMethod;
import com.elfaddoui.backend.order.repository.OrderRepository;
import com.elfaddoui.backend.order.service.OrderEventService;
import com.elfaddoui.backend.order.service.OrderService;
import com.elfaddoui.backend.product.entity.Product;
import com.elfaddoui.backend.product.repository.ProductRepository;
import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter REFERENCE_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AppConfigService appConfigService;
    private final OrderEventService orderEventService;
    private final NotificationService notificationService;

    public OrderServiceImpl(
            OrderRepository orderRepository,
            UserRepository userRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository,
            AppConfigService appConfigService,
            OrderEventService orderEventService,
            NotificationService notificationService
    ) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.appConfigService = appConfigService;
        this.orderEventService = orderEventService;
        this.notificationService = notificationService;
    }

    @Override
    public CheckoutResponse create(String userEmail, CheckoutRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByUpdatedAtDescForUpdate(user.getId());
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        PaymentMethod paymentMethod = parsePaymentMethod(request.payment().method());
        order.setPaymentMethod(paymentMethod);
        order.setStatus(resolveInitialStatus(paymentMethod));
        DeliverySlot deliverySlot = parseDeliverySlot(request.delivery().slot());
        order.setDeliverySlot(deliverySlot);
        order.setScheduledTime(resolveScheduledTime(deliverySlot, request.delivery().scheduledTime()));
        order.setFullName(request.customer().fullName().trim());
        order.setPhone(request.customer().phone().trim());
        order.setEmail(normalizeNullable(request.customer().email(), user.getEmail()));
        order.setNote(normalizeNullable(request.customer().note(), null));
        order.setCity(request.address().city().trim());
        order.setArea(request.address().area().trim());
        order.setStreet(request.address().street().trim());
        order.setExtra(normalizeNullable(request.address().extra(), null));
        order.setPostalCode(normalizeNullable(request.address().postalCode(), null));
        order.setAddressHint(normalizeNullable(request.address().hint(), null));
        order.setPlaceType(request.address().placeType().trim());
        order.setDeliveryFee(resolveDeliveryFee());
        order.setCourierName(resolveCourierName());
        order.setCourierPhone(resolveCourierPhone());
        order.setStorePhone(resolveStorePhone());

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new NotFoundException("Product not found"));
            Integer quantity = cartItem.getQuantity();
            Integer stockQty = product.getStockQty();
            BigDecimal price = getProductPrice(product);

            if (!product.isActive()) {
                throw new IllegalStateException("Product is inactive");
            }
            if (quantity == null || quantity <= 0) {
                throw new IllegalStateException("Cart contains invalid quantity");
            }
            if (stockQty == null || stockQty <= 0) {
                throw new IllegalStateException("Product is out of stock");
            }
            if (price == null) {
                throw new IllegalStateException("Product price is missing");
            }
            if (stockQty < quantity) {
                throw new IllegalStateException("Insufficient stock for product " + product.getName());
            }

            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(lineTotal);

            long currentSalesCount = product.getSalesCount() == null ? 0L : product.getSalesCount();
            product.setStockQty(stockQty - quantity);
            product.setSalesCount(currentSalesCount + quantity);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductNameSnapshot(product.getName());
            orderItem.setQty(quantity);
            orderItem.setUnitPrice(price);
            orderItem.setLineTotal(lineTotal);
            order.getItems().add(orderItem);
        }

        order.setSubtotal(subtotal);
        order.setTotal(subtotal.add(order.getDeliveryFee()));
        validateRequestedTotal(request.total(), order.getTotal());
        Order savedOrder = orderRepository.save(order);
        savedOrder.setReference(buildReference(savedOrder.getId()));
        Order persistedOrder = orderRepository.save(savedOrder);
        orderEventService.publish(persistedOrder.getReference(), toTrackingResponse(persistedOrder));
        cartItemRepository.deleteByUserId(user.getId());
        notificationService.notifyOrderCreated(persistedOrder);

        return new CheckoutResponse(
                persistedOrder.getReference(),
                persistedOrder.getStatus().name(),
                persistedOrder.getTotal()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryTrackingResponse buildTrackingResponse(Order order) {
        return toTrackingResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryTrackingResponse getTracking(String userEmail, String orderReference) {
        Order order = orderRepository.findByReferenceIgnoreCaseAndUserEmail(orderReference, userEmail)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        return toTrackingResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryTrackingResponse getLatestTracking(String userEmail) {
        Order order = orderRepository.findTopByUserEmailOrderByCreatedAtDesc(userEmail)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        return toTrackingResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public DeliverySettingsResponse getDeliverySettings() {
        return new DeliverySettingsResponse(
                resolveDeliveryFee(),
                resolveEtaLabel(),
                resolveCourierName(),
                resolveCourierPhone(),
                resolveStorePhone()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailsResponse getDetails(String userEmail, String orderReference) {
        Order order = orderRepository.findWithItemsByReferenceIgnoreCaseAndUserEmail(orderReference, userEmail)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        return new OrderDetailsResponse(
                order.getReference(),
                order.getStatus().name(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getPaymentMethod() == null ? "—" : order.getPaymentMethod().name(),
                buildDeliveryAddress(order),
                resolveDeliverySlotLabel(order),
                order.getDeliveryFee() == null ? resolveDeliveryFee() : order.getDeliveryFee(),
                normalizeOrDefault(order.getCourierName(), resolveCourierName()),
                normalizeOrDefault(order.getCourierPhone(), resolveCourierPhone()),
                normalizeOrDefault(order.getStorePhone(), resolveStorePhone()),
                (order.getItems() == null ? Collections.<OrderItem>emptyList() : order.getItems())
                        .stream()
                        .map(this::toOrderDetailsItemResponse)
                        .collect(Collectors.toList())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderHistoryItemResponse> getHistory(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toHistoryItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderHistoryItemResponse cancel(String userEmail, String orderReference) {
        Order order = orderRepository.findWithItemsByReferenceIgnoreCaseAndUserEmail(orderReference, userEmail)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return toHistoryItemResponse(order);
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order cannot be cancelled anymore");
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            int qty = item.getQty() == null ? 0 : item.getQty();

            int currentStock = product.getStockQty() == null ? 0 : product.getStockQty();
            long currentSales = product.getSalesCount() == null ? 0L : product.getSalesCount();

            product.setStockQty(currentStock + qty);
            product.setSalesCount(Math.max(0L, currentSales - qty));
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderEventService.publish(order.getReference(), toTrackingResponse(order));
        notificationService.notifyOrderStatusChanged(order);
        return toHistoryItemResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(String userEmail, String orderReference) {
        Order order = orderRepository.findWithItemsByReferenceIgnoreCaseAndUserEmail(orderReference, userEmail)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        List<String> lines = buildInvoiceLines(order);
        return buildSimplePdf(lines);
    }

    @Override
    @Transactional(readOnly = true)
    public String getInvoiceUrl(String userEmail, String orderReference) {
        orderRepository.findByReferenceIgnoreCaseAndUserEmail(orderReference, userEmail)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return "/api/orders/" + orderReference + "/invoice";
    }

    private String buildReference(Long id) {
        return "ELF-" + LocalDate.now().format(REFERENCE_DATE) + "-" + String.format("%04d", id);
    }

    private PaymentMethod parsePaymentMethod(String method) {
        String normalized = method == null ? "" : method.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "CASH", "COD", "CASH_ON_DELIVERY", "PAY_ON_DELIVERY" -> PaymentMethod.CASH;
            case "CARD", "CREDIT_CARD", "DEBIT_CARD", "ONLINE", "ONLINE_PAYMENT" -> PaymentMethod.CARD;
            default -> throw new IllegalArgumentException("Unsupported payment method");
        };
    }

    private OrderStatus resolveInitialStatus(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.CASH ? OrderStatus.CONFIRMED : OrderStatus.PENDING;
    }

    private void validateRequestedTotal(BigDecimal requestedTotal, BigDecimal serverTotal) {
        if (requestedTotal == null) {
            return;
        }
        // Intentionally non-blocking: order totals are calculated server-side (prices, fees, etc.).
        // The client may send a stale total if cart/pricing changed since the last refresh.
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private DeliverySlot parseDeliverySlot(String slot) {
        String normalized = slot == null ? "" : slot.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ASAP" -> DeliverySlot.ASAP;
            case "SCHEDULED" -> DeliverySlot.SCHEDULED;
            default -> throw new IllegalArgumentException("Unsupported delivery slot");
        };
    }

    private String resolveScheduledTime(DeliverySlot slot, String scheduledTime) {
        String normalized = normalizeNullable(scheduledTime, null);
        if (slot == DeliverySlot.SCHEDULED && normalized == null) {
            throw new IllegalArgumentException("scheduledTime is required when delivery slot is scheduled");
        }
        return slot == DeliverySlot.SCHEDULED ? normalized : null;
    }

    private String buildDeliveryAddress(Order order) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, order.getCity());
        appendPart(builder, order.getArea());
        appendPart(builder, order.getStreet());
        appendPart(builder, order.getExtra());
        return builder.toString();
    }

    private void appendPart(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(", ");
        }
        builder.append(value.trim());
    }

    private BigDecimal getProductPrice(Product product) {
        BigDecimal price = product.getPrice();
        if (price == null) {
            throw new IllegalStateException("Product price is missing");
        }
        return price;
    }

    private String resolveDeliverySlotLabel(Order order) {
        if (order.getDeliverySlot() == DeliverySlot.SCHEDULED && order.getScheduledTime() != null) {
            return order.getScheduledTime();
        }
        return resolveEtaLabel();
    }

    private DeliveryTrackingResponse toTrackingResponse(Order order) {
        return new DeliveryTrackingResponse(
                order.getId(),
                order.getReference(),
                order.getReference(),
                toTrackingStatus(order.getStatus()),
                toTrackingStep(order.getStatus()),
                3,
                toEtaMinutes(order.getStatus()),
                buildDeliveryAddress(order),
                resolveDeliverySlotLabel(order),
                order.getDeliveryFee() == null ? resolveDeliveryFee() : order.getDeliveryFee(),
                normalizeOrDefault(order.getCourierName(), resolveCourierName()),
                normalizeOrDefault(order.getCourierPhone(), resolveCourierPhone()),
                normalizeOrDefault(order.getStorePhone(), resolveStorePhone())
        );
    }

    private OrderHistoryItemResponse toHistoryItemResponse(Order order) {
        return new OrderHistoryItemResponse(
                order.getId(),
                order.getReference(),
                order.getStatus().name(),
                order.getTotal(),
                order.getCreatedAt(),
                order.getItems() == null ? 0 : order.getItems().size()
        );
    }

    private OrderDetailsItemResponse toOrderDetailsItemResponse(OrderItem item) {
        return new OrderDetailsItemResponse(
                item.getProduct() == null ? null : item.getProduct().getId(),
                normalizeOrDefault(item.getProductNameSnapshot(), "Produit"),
                item.getQty(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }

    private int toTrackingStep(OrderStatus status) {
        return switch (status) {
            case PENDING, CONFIRMED, PREPARING -> 0;
            case SHIPPED -> 1;
            case DELIVERED, CANCELLED -> 2;
        };
    }

    private String toTrackingStatus(OrderStatus status) {
        return switch (status) {
            case PENDING, CONFIRMED, PREPARING -> "preparing";
            case SHIPPED -> "on_route";
            case DELIVERED -> "delivered";
            case CANCELLED -> "cancelled";
        };
    }

    private Integer toEtaMinutes(OrderStatus status) {
        return switch (status) {
            case PENDING, CONFIRMED, PREPARING -> 35;
            case SHIPPED -> 18;
            case DELIVERED, CANCELLED -> null;
        };
    }

    private String normalizeOrDefault(String value, String fallback) {
        String normalized = normalizeNullable(value, null);
        if (normalized == null) {
            return fallback;
        }
        return normalized;
    }

    private String normalizeNullable(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? fallback : trimmed;
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

    private String resolveCourierName() {
        try {
            return appConfigService.getValue(
                    AdminBackofficeServiceImpl.DELIVERY_COURIER_NAME_KEY,
                    AdminBackofficeServiceImpl.DEFAULT_COURIER_NAME
            );
        } catch (Exception ignored) {
            return AdminBackofficeServiceImpl.DEFAULT_COURIER_NAME;
        }
    }

    private String resolveCourierPhone() {
        try {
            return appConfigService.getValue(
                    AdminBackofficeServiceImpl.DELIVERY_COURIER_PHONE_KEY,
                    AdminBackofficeServiceImpl.DEFAULT_COURIER_PHONE
            );
        } catch (Exception ignored) {
            return AdminBackofficeServiceImpl.DEFAULT_COURIER_PHONE;
        }
    }

    private String resolveEtaLabel() {
        try {
            return appConfigService.getValue(
                    AdminBackofficeServiceImpl.DELIVERY_ETA_LABEL_KEY,
                    AdminBackofficeServiceImpl.DEFAULT_ETA_LABEL
            );
        } catch (Exception ignored) {
            return AdminBackofficeServiceImpl.DEFAULT_ETA_LABEL;
        }
    }

    private String resolveStorePhone() {
        try {
            return appConfigService.getValue(
                    AdminBackofficeServiceImpl.DELIVERY_STORE_PHONE_KEY,
                    AdminBackofficeServiceImpl.DEFAULT_STORE_PHONE
            );
        } catch (Exception ignored) {
            return AdminBackofficeServiceImpl.DEFAULT_STORE_PHONE;
        }
    }

    private List<String> buildInvoiceLines(Order order) {
        List<String> itemLines = (order.getItems() == null ? Collections.<OrderItem>emptyList() : order.getItems())
                .stream()
                .map(item -> String.format(
                        Locale.ROOT,
                        "- %s x%s : %s TND",
                        normalizeOrDefault(item.getProductNameSnapshot(), "Produit"),
                        item.getQty() == null ? 0 : item.getQty(),
                        formatMoney(item.getLineTotal())
                ))
                .toList();

        List<String> lines = new java.util.ArrayList<>();
        lines.add("Facture commande " + normalizeOrDefault(order.getReference(), "-"));
        lines.add("Date: " + (order.getCreatedAt() == null ? "-" : order.getCreatedAt().toString()));
        lines.add("Client: " + normalizeOrDefault(order.getFullName(), "-"));
        lines.add("Telephone: " + normalizeOrDefault(order.getPhone(), "-"));
        lines.add("Adresse: " + buildDeliveryAddress(order));
        lines.add("Paiement: " + (order.getPaymentMethod() == null ? "-" : order.getPaymentMethod().name()));
        lines.add("Statut: " + (order.getStatus() == null ? "-" : order.getStatus().name()));
        lines.add(" ");
        lines.add("Articles:");
        if (itemLines.isEmpty()) {
            lines.add("- Aucun article");
        } else {
            lines.addAll(itemLines);
        }
        lines.add(" ");
        lines.add("Sous-total: " + formatMoney(order.getSubtotal()) + " TND");
        lines.add("Livraison: " + formatMoney(order.getDeliveryFee()) + " TND");
        lines.add("Total: " + formatMoney(order.getTotal()) + " TND");
        return lines;
    }

    private String formatMoney(BigDecimal amount) {
        return amount == null ? "0.00" : amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }

    private byte[] buildSimplePdf(List<String> lines) {
        StringBuilder content = new StringBuilder("BT\n/F1 11 Tf\n40 780 Td\n14 TL\n");
        boolean first = true;
        for (String line : lines) {
            if (!first) {
                content.append("T*\n");
            }
            content.append("(").append(escapePdfText(line)).append(") Tj\n");
            first = false;
        }
        content.append("ET\n");

        byte[] contentBytes = content.toString().getBytes(StandardCharsets.US_ASCII);
        String header = "%PDF-1.4\n";
        String object1 = "1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj\n";
        String object2 = "2 0 obj << /Type /Pages /Count 1 /Kids [3 0 R] >> endobj\n";
        String object3 = "3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >> endobj\n";
        String object4 = "4 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj\n";
        String object5Prefix = "5 0 obj << /Length " + contentBytes.length + " >> stream\n";
        String object5Suffix = "endstream\nendobj\n";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeAscii(out, header);

        List<Integer> offsets = new java.util.ArrayList<>();
        offsets.add(out.size());
        writeAscii(out, object1);
        offsets.add(out.size());
        writeAscii(out, object2);
        offsets.add(out.size());
        writeAscii(out, object3);
        offsets.add(out.size());
        writeAscii(out, object4);
        offsets.add(out.size());
        writeAscii(out, object5Prefix);
        out.writeBytes(contentBytes);
        writeAscii(out, "\n" + object5Suffix);

        int xrefOffset = out.size();
        writeAscii(out, "xref\n0 6\n");
        writeAscii(out, "0000000000 65535 f \n");
        for (Integer offset : offsets) {
            writeAscii(out, String.format(Locale.ROOT, "%010d 00000 n \n", offset));
        }
        writeAscii(out, "trailer << /Size 6 /Root 1 0 R >>\n");
        writeAscii(out, "startxref\n" + xrefOffset + "\n%%EOF");
        return out.toByteArray();
    }

    private void writeAscii(ByteArrayOutputStream out, String value) {
        out.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }

    private String escapePdfText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
        StringBuilder sanitized = new StringBuilder(normalized.length());
        for (int i = 0; i < normalized.length(); i++) {
            char current = normalized.charAt(i);
            sanitized.append(current <= 127 ? current : '?');
        }
        return sanitized.toString();
    }
}
