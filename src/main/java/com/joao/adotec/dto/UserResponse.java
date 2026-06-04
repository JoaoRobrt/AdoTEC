package com.joao.adotec.dto;

import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String email,
        Boolean isActive,
        List<String> roles
) {
    /** Construtor legado sem status/roles — mantém compatibilidade com código existente. */
    public UserResponse(Long id, String name, String email) {
        this(id, name, email, true, List.of());
    }
}
