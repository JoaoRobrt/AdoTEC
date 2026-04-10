package com.joao.adotec.controllers;

import com.joao.adotec.dto.AppointmentRequestDTO;
import com.joao.adotec.dto.AppointmentResponseDTO;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.mappers.AppointmentMapper;
import com.joao.adotec.models.Appointment;
import com.joao.adotec.security.services.UserDetailsImpl;
import com.joao.adotec.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper = AppointmentMapper.INSTANCE;

    @PostMapping
    @PreAuthorize("hasRole('ADOPTER')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> createAppointment(
            @Valid @RequestBody AppointmentRequestDTO request,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long adopterId = userDetails.getId();

        Appointment appointment = appointmentService.createAppointment(adopterId, request.getPetId(), request.getTimeSlotId());
        AppointmentResponseDTO responseData = appointmentMapper.toDTO(appointment);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment scheduled successfully", responseData));
    }

    @PatchMapping("/{id}/assign/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> assignEmployee(
            @PathVariable Long id,
            @PathVariable Long employeeId) {

        Appointment appointment = appointmentService.assignEmployee(id, employeeId);
        AppointmentResponseDTO responseData = appointmentMapper.toDTO(appointment);

        return ResponseEntity.ok(ApiResponse.success("Employee assigned successfully", responseData));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADOPTER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getMyAppointments(
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        
        boolean isEmployee = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE"));

        List<Appointment> appointments;
        if (isEmployee) {
            appointments = appointmentService.getAppointmentsByEmployee(userId);
        } else {
            appointments = appointmentService.getAppointmentsByAdopter(userId);
        }

        List<AppointmentResponseDTO> responseData = appointmentMapper.toDTOList(appointments);

        return ResponseEntity.ok(ApiResponse.success("Appointments fetched successfully", responseData));
    }
}
