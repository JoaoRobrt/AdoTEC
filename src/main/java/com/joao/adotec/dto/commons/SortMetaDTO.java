package com.joao.adotec.dto.commons;

import org.springframework.data.domain.Sort;

public record SortMetaDTO(String property, String direction) {

    public static SortMetaDTO fromSort(Sort.Order order) {
        if (order == null) return null;
        return new SortMetaDTO(
                order.getProperty(),
                order.getDirection().name()
        );
    }
}
