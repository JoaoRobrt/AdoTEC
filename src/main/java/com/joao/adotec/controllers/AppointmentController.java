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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Endpoints for managing adoption appointments/visits")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper = AppointmentMapper.INSTANCE;

    @Operation(summary = "Create an appointment", description = "Allows an Adopter to schedule a visit for a specific pet in a given time slot.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Appointment scheduled successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error in request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet or TimeSlot not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Business rule violation (e.g. time slot full or duplicate assignment)")
    })
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

    @Operation(summary = "Assign employee to an appointment", description = "Allows an Admin to assign an employee to accompany an upcoming visit.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee assigned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Appointment or Employee not found")
    })
    @PatchMapping("/{id}/assign/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> assignEmployee(
            @PathVariable Long id,
            @PathVariable Long employeeId) {

        Appointment appointment = appointmentService.assignEmployee(id, employeeId);
        AppointmentResponseDTO responseData = appointmentMapper.toDTO(appointment);

        return ResponseEntity.ok(ApiResponse.success("Employee assigned successfully", responseData));
    }

    @Operation(summary = "Get my appointments", description = "Retrieves all appointments belonging to the currently logged in user (either Adopter or Employee).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointments fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
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

    @Operation(summary = "Get all appointments", description = "Retrieves a listing of all appointments in the system. Requires ADMIN role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All appointments fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponseDTO>>> getAllAppointments() {
        List<Appointment> appointments = appointmentService.getAllAppointments();
        List<AppointmentResponseDTO> responseData = appointmentMapper.toDTOList(appointments);

        return ResponseEntity.ok(ApiResponse.success("All appointments fetched successfully", responseData));
    }

    @Operation(summary = "Register adoption result", description = "Registers the final result (APPROVED or REJECTED) of an appointment visit.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Appointment result registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error in request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Appointment not found")
    })
    @PatchMapping("/{id}/result")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> registerResult(
            @PathVariable Long id,
            @Valid @RequestBody com.joao.adotec.dto.AppointmentResultDTO resultDto) {

        Appointment appointment = appointmentService.registerResult(id, resultDto);
        AppointmentResponseDTO responseData = appointmentMapper.toDTO(appointment);

        return ResponseEntity.ok(ApiResponse.success("Appointment result registered successfully", responseData));
    }
}
