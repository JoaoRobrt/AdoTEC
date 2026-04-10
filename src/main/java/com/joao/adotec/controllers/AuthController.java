package com.joao.adotec.controllers;

import com.joao.adotec.dto.LoginRequest;
import com.joao.adotec.dto.SignupRequest;
import com.joao.adotec.dto.UserInfoResponse;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Authenticate user", description = "Authenticates a user with email and password, returning a JWT token with user info.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserInfoResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        UserInfoResponse userInfoResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("User authenticated successfully", userInfoResponse));
    }

    @Operation(summary = "Register user", description = "Registers a new user (adopter) in the system.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Email already in use or validation error")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserInfoResponse>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        UserInfoResponse userInfoResponse = authService.registerUser(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User registered successfully", userInfoResponse));
    }
}
