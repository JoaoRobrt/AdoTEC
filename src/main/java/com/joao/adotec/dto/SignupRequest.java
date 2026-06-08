package com.joao.adotec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Dados para registro de uma nova conta de adotante")
public class SignupRequest {
    
    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Nome completo do adotante", example = "Adopter User")
    private String name;

    @NotBlank
    @Email
    @Schema(description = "E-mail de acesso exclusivo do adotante", example = "adopter@adotec.com")
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    @Schema(description = "Senha de acesso (mínimo de 6 caracteres)", example = "adopter123")
    private String password;
}
