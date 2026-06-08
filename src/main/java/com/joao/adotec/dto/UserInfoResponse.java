package com.joao.adotec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Resposta contendo informações do usuário autenticado e o token de acesso")
public class UserInfoResponse {
    
    @Schema(description = "ID do usuário autenticado", example = "1")
    private Long id;
    
    @Schema(description = "Nome do usuário", example = "Admin User")
    private String name;
    
    @Schema(description = "E-mail cadastrado", example = "admin@adotec.com")
    private String email;
    
    @Schema(description = "Lista de perfis de permissão (Roles) associados ao usuário", example = "[\"ROLE_ADMIN\"]")
    private List<String> roles;
    
    @Schema(description = "Token JWT a ser enviado no header Authorization de requisições protegidas", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pb...")
    private String jwtToken;
}
