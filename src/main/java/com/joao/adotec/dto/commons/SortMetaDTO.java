package com.joao.adotec.dto.commons;

import org.springframework.data.domain.Sort;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representa o estado de ordenação de um atributo específico")
public record SortMetaDTO(
        @Schema(description = "Nome do atributo ordenado no banco de dados", example = "createdAt")
        String property,
        
        @Schema(description = "Direção da ordenação aplicada: ASC (Crescente) ou DESC (Decrescente)", example = "DESC")
        String direction
) {

    public static SortMetaDTO fromSort(Sort.Order order) {
        if (order == null) return null;
        return new SortMetaDTO(
                order.getProperty(),
                order.getDirection().name()
        );
    }
}
