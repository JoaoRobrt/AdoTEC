package com.joao.adotec.dto;

import java.time.Instant;

public record PetPhotoResponseDTO(
        Long photoId,
        String url,
        Boolean isPrimary,
        Instant createdAt
) {
}
