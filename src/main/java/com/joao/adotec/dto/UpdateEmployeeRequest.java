package com.joao.adotec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Dados para atualização de perfil do funcionário")
public class UpdateEmployeeRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Nome do funcionário", example = "Employee User")
    private String name;

    @NotBlank
    @Email
    @Schema(description = "E-mail do funcionário", example = "employee@adotec.com")
    private String email;
}
