package com.rakeshgupta.cafebrew_backend.admin.service;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.AdminLoginRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.response.AdminLoginResponse;
import com.rakeshgupta.cafebrew_backend.admin.entity.AdminUser;
import com.rakeshgupta.cafebrew_backend.admin.repository.AdminUserRepository;
import com.rakeshgupta.cafebrew_backend.common.enums.AdminRole;
import com.rakeshgupta.cafebrew_backend.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * Authenticate admin user and generate token
     */
    public AdminLoginResponse login(AdminLoginRequest request) {
        
        AdminUser user = adminUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        
        return new AdminLoginResponse(token, user.getRole());
    }
    
    /**
     * Refresh JWT token
     * Validates existing token and issues a new one
     */
    public AdminLoginResponse refreshToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        
        String username = jwtTokenProvider.getUsername(token);
        String roleStr = jwtTokenProvider.getRole(token);
        AdminRole role = AdminRole.valueOf(roleStr);
        
        // Verify user still exists and is active
        adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        String newToken = jwtTokenProvider.generateToken(username, roleStr);
        
        return new AdminLoginResponse(newToken, role);
    }
}