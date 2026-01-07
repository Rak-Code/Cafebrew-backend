package com.rakeshgupta.cafebrew_backend.admin.controller;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.AdminLoginRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.response.AdminLoginResponse;
import com.rakeshgupta.cafebrew_backend.admin.service.AdminAuthService;
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
}
