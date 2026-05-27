package com.elfaddoui.backend.notification.repository;

import com.elfaddoui.backend.notification.entity.InAppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {

    List<InAppNotification> findByUserEmailOrderByCreatedAtDesc(String email);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update InAppNotification notification
            set notification.unread = false
            where notification.user.email = :email and notification.unread = true
            """)
    int markAllRead(@Param("email") String email);
}
