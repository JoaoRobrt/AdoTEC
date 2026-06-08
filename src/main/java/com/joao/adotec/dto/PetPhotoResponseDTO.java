package com.joao.adotec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Detalhes de uma foto cadastrada para o pet")
public record PetPhotoResponseDTO(
        @Schema(description = "ID único da foto", example = "1")
        Long photoId,
        
        @Schema(description = "URL completa da foto hospedada no Cloudinary ou seed de desenvolvimento", example = "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=600&h=600&fit=crop")
        String url,
        
        @Schema(description = "Determina se esta é a foto principal exibida nos cards e listagens", example = "true")
        Boolean isPrimary,
        
        @Schema(description = "Data e hora em que o upload da foto foi realizado", example = "2026-06-08T18:54:00Z")
        Instant createdAt
) {
}
