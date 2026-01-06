package com.rakeshgupta.cafebrew_backend.customer.controller;

import com.rakeshgupta.cafebrew_backend.customer.dto.response.MenuItemResponse;
import com.rakeshgupta.cafebrew_backend.customer.entity.MenuItem;
import com.rakeshgupta.cafebrew_backend.customer.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * GET /api/menu
     * Public endpoint - returns ONLY available menu items
     */
    @GetMapping
    public ResponseEntity<List<MenuItem>> getMenu() {
        List<MenuItem> menu = menuService.getAvailableMenu();
        return ResponseEntity.ok(menu);
    }
}
