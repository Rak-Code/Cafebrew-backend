package com.rakeshgupta.cafebrew_backend.admin.controller;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.CreateMenuItemRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.request.ToggleMenuAvailabilityRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.request.UpdateMenuItemRequest;
import com.rakeshgupta.cafebrew_backend.admin.dto.response.MenuItemResponse;
import com.rakeshgupta.cafebrew_backend.customer.entity.MenuItem;
import com.rakeshgupta.cafebrew_backend.customer.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
     * GET /api/admin/menu/{id}
     * Get a single menu item by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Long id) {
        MenuItem menuItem = menuService.getMenuItemById(id);
        return ResponseEntity.ok(MenuItemResponse.fromEntity(menuItem));
    }

    /**
     * POST /api/admin/menu
     * Create a new menu item
     */
    @PostMapping
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {
        MenuItem created = menuService.createMenuItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(MenuItemResponse.fromEntity(created));
    }

    /**
     * PUT /api/admin/menu/{id}
     * Update an existing menu item
     */
    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        MenuItem updated = menuService.updateMenuItem(id, request);
        return ResponseEntity.ok(MenuItemResponse.fromEntity(updated));
    }

    /**
     * DELETE /api/admin/menu/{id}
     * Delete a menu item
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
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
