package com.example.jobportal.repository;

import com.example.jobportal.entity.User;
import com.example.jobportal.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByRoleIn(Collection<Role> roles);

    @Query("""
            SELECT u FROM User u
            WHERE (:role IS NULL OR u.role = :role)
              AND (:keyword IS NULL OR
                   LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<User> search(@Param("role") Role role,
                      @Param("keyword") String keyword,
                      Pageable pageable);
}
