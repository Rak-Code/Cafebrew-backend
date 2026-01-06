package com.rakeshgupta.cafebrew_backend.admin.entity;

import com.rakeshgupta.cafebrew_backend.common.enums.AdminRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password; // BCrypt encoded
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public AdminUser(String username, String password, AdminRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}