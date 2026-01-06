package com.rakeshgupta.cafebrew_backend.admin.controller;

import com.rakeshgupta.cafebrew_backend.admin.dto.request.ToggleMenuAvailabilityRequest;
import com.rakeshgupta.cafebrew_backend.customer.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/menu")
@RequiredArgsConstructor
public class AdminMenuController {

    private final MenuService menuService;

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
