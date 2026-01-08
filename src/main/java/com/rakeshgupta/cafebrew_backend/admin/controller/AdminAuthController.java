package com.rakeshgupta.cafebrew_backend.admin.controller;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.AdminLoginRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.response.AdminLoginResponse;
import com.rakeshgupta.cafebrew_backend.admin.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        AdminLoginResponse response = adminAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/admin/refresh-token
     * Refresh JWT token before it expires
     * Requires valid (non-expired) token in Authorization header
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AdminLoginResponse> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        
        String token = authHeader.substring(7);
        AdminLoginResponse response = adminAuthService.refreshToken(token);
        return ResponseEntity.ok(response);
    }
}
