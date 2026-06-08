package com.joao.adotec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credenciais para login de usuário")
public class LoginRequest {
    
    @NotBlank
    @Email
    @Schema(description = "E-mail cadastrado do usuário", example = "admin@adotec.com")
    private String email;

    @NotBlank
    @Schema(description = "Senha do usuário", example = "admin123")
    private String password;
}
