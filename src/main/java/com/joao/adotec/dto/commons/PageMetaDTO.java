package com.joao.adotec.dto.commons;

import org.springframework.data.domain.Page;
import java.util.List;

public record PageMetaDTO(
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
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
