package com.elfaddoui.backend.product.repository;

import com.elfaddoui.backend.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findTop6ByActiveTrueAndDiscountPctGreaterThanOrderByDiscountPctDescUpdatedAtDesc(int discountPct);
    List<Product> findTop8ByActiveTrueOrderByRatingDescSalesCountDescUpdatedAtDesc();
    List<Product> findTop6ByActiveTrueOrderByCreatedAtDesc();

    @Query("""
            select p from Product p
            where p.active = true
              and p.promo = true
              and p.discountPct > 0
              and (p.promoStartsAt is null or p.promoStartsAt <= :now)
              and (p.promoEndsAt is null or p.promoEndsAt >= :now)
            order by p.discountPct desc, p.updatedAt desc
            """)
    List<Product> findActiveHomeDeals(@Param("now") Instant now, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Query("select p from Product p join fetch p.category")
    List<Product> findAllWithCategoryForRag();
}
