package com.joao.adotec.controllers;

import com.joao.adotec.dto.CreateEmployeeRequest;
import com.joao.adotec.dto.UpdateEmployeeRequest;
import com.joao.adotec.dto.UserResponse;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.security.services.UserDetailsImpl;
import com.joao.adotec.services.UserService;
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
@RequestMapping("/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Employees", description = "CRUD endpoints for managing employees (ADMIN only)")
public class EmployeeController {

    private final UserService userService;

    @Operation(summary = "List employees", description = "Returns all users with ROLE_EMPLOYEE.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employees retrieved")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getEmployees() {
        List<UserResponse> employees = userService.findEmployees();
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }

    @Operation(summary = "Get employee by ID", description = "Returns a single employee/admin by ID.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getEmployeeById(@PathVariable Long id) {
        UserResponse employee = userService.findEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", employee));
    }

    @Operation(summary = "Create employee", description = "Creates a new employee or admin user.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Employee created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        UserResponse created = userService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", created));
    }

    @Operation(summary = "Update employee", description = "Updates name and email of an employee.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        UserResponse updated = userService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updated));
    }

    @Operation(summary = "Toggle employee active status", description = "Activates or deactivates an employee (soft delete).")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status toggled"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot deactivate yourself"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<UserResponse>> toggleActive(
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserResponse toggled = userService.toggleActive(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Employee status toggled successfully", toggled));
    }
}
