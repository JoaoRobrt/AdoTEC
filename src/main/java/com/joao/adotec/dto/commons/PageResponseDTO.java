package com.joao.adotec.dto.commons;

import java.util.List;

public record PageResponseDTO<T>(
        List<T> content,
        PageMetaDTO pagination
) {
}
