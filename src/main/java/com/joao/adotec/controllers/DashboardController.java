package com.joao.adotec.controllers;

import com.joao.adotec.dto.AppointmentResponseDTO;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.dto.commons.PageMetaDTO;
import com.joao.adotec.dto.commons.PageResponseDTO;
import com.joao.adotec.mappers.AppointmentMapper;
import com.joao.adotec.models.Appointment;
import com.joao.adotec.services.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    @Operation(summary = "Get unassigned appointments", description = "Retrieves a listing of appointments that are pending assignment. Requires ADMIN role.")
    @GetMapping("/unassigned-appointments")
    public ResponseEntity<ApiResponse<PageResponseDTO<AppointmentResponseDTO>>> getUnassignedAppointments(
            @PageableDefault(size = 5, page = 0) Pageable pageable) {
        
        Page<Appointment> appointmentsPage = appointmentService.getUnassignedAppointments(pageable);
        Page<AppointmentResponseDTO> mappedPage = appointmentsPage.map(appointmentMapper::toDTO);

        PageResponseDTO<AppointmentResponseDTO> pageResponse = new PageResponseDTO<>(
                mappedPage.getContent(),
                PageMetaDTO.fromPage(mappedPage)
        );

        return ResponseEntity.ok(ApiResponse.success("Unassigned appointments fetched successfully", pageResponse));
    }
}
