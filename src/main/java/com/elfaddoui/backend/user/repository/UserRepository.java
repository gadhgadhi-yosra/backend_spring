package com.elfaddoui.backend.user.repository;

import com.elfaddoui.backend.user.entity.User;
import com.elfaddoui.backend.user.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<User> findByResetToken(String resetToken);

    @Query("select distinct u from User u join u.roles r where r = :role order by u.id desc")
    Page<User> findByRole(@Param("role") Role role, Pageable pageable);

    @Query("""
            select distinct u
            from User u join u.roles r
            where r = :role
              and (
                   lower(u.fullName) like lower(concat('%', :q, '%'))
                   or lower(u.email) like lower(concat('%', :q, '%'))
              )
            order by u.id desc
            """)
    Page<User> searchByRoleAndQuery(@Param("role") Role role, @Param("q") String q, Pageable pageable);
}
