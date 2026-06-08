package com.joao.adotec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Dados para cadastro de um novo funcionário ou administrador")
public class CreateEmployeeRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Nome completo do funcionário", example = "Employee User")
    private String name;

    @NotBlank
    @Email
    @Schema(description = "E-mail de acesso (único no sistema)", example = "employee@adotec.com")
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    @Schema(description = "Senha de login (mínimo de 6 caracteres)", example = "employee123")
    private String password;

    @Schema(description = "Role a atribuir: 'EMPLOYEE' ou 'ADMIN'. Se nulo ou vazio, assume 'EMPLOYEE'.", example = "EMPLOYEE")
    private String role;
}
