package com.rakeshgupta.cafebrew_backend.admin.controller;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.ToggleMenuAvailabilityRequest;
import com.rakeshgupta.cafebrew_backend.customer.entity.MenuItem;
import com.rakeshgupta.cafebrew_backend.customer.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menu")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;

    /**
     * GET /api/admin/menu
     * Returns ALL menu items (including unavailable) for admin management
     */
    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllMenuItems() {
        List<MenuItem> allItems = menuService.getAllMenuItems();
        return ResponseEntity.ok(allItems);
    }

    /**
     * PUT /api/admin/menu/{id}/availability
     * Toggle menu item availability
     */
    @PutMapping("/{id}/availability")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long id,
            @RequestBody ToggleMenuAvailabilityRequest request
    ) {
        menuService.updateAvailability(id, request.getAvailable());
        return ResponseEntity.ok().build();
    }
}
