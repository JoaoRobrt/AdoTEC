package com.joao.adotec.controllers;

import com.joao.adotec.dto.AppointmentResponseDTO;
import com.joao.adotec.dto.DashboardMetricsDTO;
import com.joao.adotec.dto.commons.PageMetaDTO;
import com.joao.adotec.dto.commons.PageResponseDTO;
import com.joao.adotec.dto.commons.DashboardUnassignedCacheWrapper;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for dashboard metrics and operational lists")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get dashboard metrics", description = "Retrieves consolidated statistics for the admin dashboard. Requires ADMIN role.")
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<DashboardMetricsDTO>> getDashboardMetrics() {
        DashboardMetricsDTO metrics = dashboardService.getDashboardMetrics();
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics fetched successfully", metrics));
    }

    @Operation(summary = "Get unassigned appointments", description = "Retrieves a listing of appointments that are pending assignment. Requires ADMIN role.")
    @GetMapping("/unassigned-appointments")
    public ResponseEntity<ApiResponse<PageResponseDTO<AppointmentResponseDTO>>> getUnassignedAppointments() {
        DashboardUnassignedCacheWrapper cached = dashboardService.getUnassignedAppointments();
        
        // Wrap the list in a PageResponseDTO for frontend compatibility
        int totalElements = cached.getAppointments().size();
        PageMetaDTO meta = new PageMetaDTO(
                0,
                5,
                (long) totalElements,
                1,
                true,
                true,
                java.util.Collections.emptyList()
        );
        PageResponseDTO<AppointmentResponseDTO> pageResponse = new PageResponseDTO<>(
                cached.getAppointments(),
                meta
        );

        return ResponseEntity.ok(ApiResponse.success("Unassigned appointments fetched successfully", pageResponse));
    }
}
