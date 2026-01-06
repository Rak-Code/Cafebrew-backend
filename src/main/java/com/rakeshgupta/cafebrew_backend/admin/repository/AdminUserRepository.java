package com.rakeshgupta.cafebrew_backend.admin.repository;

import com.rakeshgupta.cafebrew_backend.admin.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    
    /**
     * Find admin user by username for authentication
     */
    Optional<AdminUser> findByUsername(String username);
}