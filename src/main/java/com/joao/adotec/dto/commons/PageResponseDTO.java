package com.joao.adotec.dto.commons;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Resposta envelopada padrão para resultados paginados")
public record PageResponseDTO<T>(
        @Schema(description = "Lista contendo os registros retornados na página atual")
        List<T> content,
        
        @Schema(description = "Metadados adicionais referentes à paginação")
        PageMetaDTO pagination
) {
}
