package com.rakeshgupta.cafebrew_backend.admin.service;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.AdminLoginRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.response.AdminLoginResponse;
import com.rakeshgupta.cafebrew_backend.admin.entity.AdminUser;
import com.rakeshgupta.cafebrew_backend.admin.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {
    
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Authenticate admin user and generate token
     */
    public AdminLoginResponse login(AdminLoginRequest request) {
        
        AdminUser user = adminUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        // TODO: Integrate JwtTokenProvider
        // String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole().name());
        String token = generateToken(user);
        
        return new AdminLoginResponse(token, user.getRole());
    }
    
    private String generateToken(AdminUser adminUser) {
        // TODO: Implement JWT token generation with JwtTokenProvider
        return "admin_token_" + adminUser.getId() + "_" + System.currentTimeMillis();
    }
}