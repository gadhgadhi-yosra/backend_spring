package com.elfaddoui.backend.auth.service;

public interface ResetOtpDeliveryService {
    void sendResetOtp(String email, String otp);
}
