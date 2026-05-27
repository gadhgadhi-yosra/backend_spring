package com.elfaddoui.backend.notification.repository;

import com.elfaddoui.backend.notification.entity.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {
    Optional<UserDeviceToken> findByToken(String token);
}
