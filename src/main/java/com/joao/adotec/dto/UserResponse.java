package com.joao.adotec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Dados detalhados do perfil de um usuário")
public record UserResponse(
        @Schema(description = "ID do usuário no sistema", example = "1")
        Long id,
        
        @Schema(description = "Nome completo do usuário", example = "Admin User")
        String name,
        
        @Schema(description = "E-mail cadastrado", example = "admin@adotec.com")
        String email,
        
        @Schema(description = "Determina se a conta do usuário está ativa no sistema", example = "true")
        Boolean isActive,
        
        @Schema(description = "Lista de perfis de permissão (Roles) associados ao usuário", example = "[\"ROLE_ADMIN\"]")
        List<String> roles
) {
    /** Construtor legado sem status/roles — mantém compatibilidade com código existente. */
    public UserResponse(Long id, String name, String email) {
        this(id, name, email, true, List.of());
    }
}
