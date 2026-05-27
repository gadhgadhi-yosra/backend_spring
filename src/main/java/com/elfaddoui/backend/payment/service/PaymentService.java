package com.elfaddoui.backend.payment.service;

import com.elfaddoui.backend.payment.dto.PaymentIntentRequest;
import com.elfaddoui.backend.payment.dto.PaymentIntentResponse;

public interface PaymentService {

    PaymentIntentResponse createIntent(String userEmail, PaymentIntentRequest request);
}
