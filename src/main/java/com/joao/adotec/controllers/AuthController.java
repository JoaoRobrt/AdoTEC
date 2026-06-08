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
@Tag(name = "Authentication", description = "Endpoints para autenticação de usuários e registro de novos adotantes")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Autenticar usuário (Login)",
            description = "Valida o e-mail e senha informados. Retorna as informações do usuário autenticado e o token JWT a ser utilizado nas requisições protegidas."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserInfoResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        UserInfoResponse userInfoResponse = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("User authenticated successfully", userInfoResponse));
    }

    @Operation(
            summary = "Registrar novo adotante",
            description = "Cadastra um novo usuário adotante no sistema (ROLE_ADOPTER). O e-mail informado deve ser único."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Cadastro realizado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "E-mail já cadastrado ou payload inválido")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserInfoResponse>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        UserInfoResponse userInfoResponse = authService.registerUser(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User registered successfully", userInfoResponse));
    }

    @Operation(
            summary = "Obter dados do usuário atual",
            description = "Retorna os dados do usuário autenticado a partir do token JWT enviado no cabeçalho Authorization."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Não autenticado ou token inválido")
    })
    @org.springframework.web.bind.annotation.GetMapping("/me")
    public ResponseEntity<ApiResponse<com.joao.adotec.dto.UserResponse>> getCurrentUser(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.joao.adotec.security.services.UserDetailsImpl userDetails) {
        
        com.joao.adotec.dto.UserResponse response = new com.joao.adotec.dto.UserResponse(
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getEmail()
        );
        return ResponseEntity.ok(ApiResponse.success("User info retrieved successfully", response));
    }
}
