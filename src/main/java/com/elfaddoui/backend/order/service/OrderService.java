package com.elfaddoui.backend.order.service;

import com.elfaddoui.backend.order.dto.CheckoutRequest;
import com.elfaddoui.backend.order.dto.CheckoutResponse;
import com.elfaddoui.backend.order.dto.DeliverySettingsResponse;
import com.elfaddoui.backend.order.dto.DeliveryTrackingResponse;
import com.elfaddoui.backend.order.dto.OrderDetailsResponse;
import com.elfaddoui.backend.order.dto.OrderHistoryItemResponse;
import com.elfaddoui.backend.order.entity.Order;

import java.util.List;

public interface OrderService {
    CheckoutResponse create(String userEmail, CheckoutRequest request);
    DeliveryTrackingResponse buildTrackingResponse(Order order);
    DeliveryTrackingResponse getTracking(String userEmail, String orderReference);
    DeliveryTrackingResponse getLatestTracking(String userEmail);
    DeliverySettingsResponse getDeliverySettings();
    OrderDetailsResponse getDetails(String userEmail, String orderReference);
    List<OrderHistoryItemResponse> getHistory(String userEmail);
    OrderHistoryItemResponse cancel(String userEmail, String orderReference);
    byte[] generateInvoicePdf(String userEmail, String orderReference);
    String getInvoiceUrl(String userEmail, String orderReference);
}
