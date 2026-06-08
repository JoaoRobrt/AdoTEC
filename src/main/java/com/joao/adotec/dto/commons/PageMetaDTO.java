package com.joao.adotec.dto.commons;

import org.springframework.data.domain.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Metadados de detalhamento da paginação do Spring Data")
public record PageMetaDTO(
        @Schema(description = "Índice da página atual (inicia em 0)", example = "0")
        int number,
        
        @Schema(description = "Quantidade de itens solicitados por página", example = "10")
        int size,
        
        @Schema(description = "Total de registros encontrados no banco de dados com base nos filtros", example = "52")
        long totalElements,
        
        @Schema(description = "Total de páginas disponíveis para navegação", example = "6")
        int totalPages,
        
        @Schema(description = "Indica se a página atual é a primeira", example = "true")
        boolean first,
        
        @Schema(description = "Indica se a página atual é a última", example = "false")
        boolean last,
        
        @Schema(description = "Estrutura contendo o status de ordenação aplicado na pesquisa")
        List<SortMetaDTO> sort
) {

    public static PageMetaDTO fromPage(Page<?> page) {
        if (page == null) return null;
        List<SortMetaDTO> sortInfo = page.getSort()
                .stream()
                .map(SortMetaDTO::fromSort)
                .toList();

        return new PageMetaDTO(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                sortInfo
        );
    }
}
