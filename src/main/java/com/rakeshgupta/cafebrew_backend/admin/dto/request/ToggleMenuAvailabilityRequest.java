package com.rakeshgupta.cafebrew_backend.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToggleMenuAvailabilityRequest {
    
    @NotNull(message = "Availability flag is required")
    private Boolean available;
}