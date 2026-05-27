package com.elfaddoui.backend.cart.repository;

import com.elfaddoui.backend.cart.entity.CartItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CartItem c where c.user.id = :userId order by c.updatedAt desc")
    List<CartItem> findByUserIdOrderByUpdatedAtDescForUpdate(@Param("userId") Long userId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    long deleteByUserIdAndProductId(Long userId, Long productId);
    long deleteByUserId(Long userId);
}
