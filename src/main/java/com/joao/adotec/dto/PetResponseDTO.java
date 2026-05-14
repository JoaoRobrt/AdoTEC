package com.joao.adotec.dto;

import com.joao.adotec.enums.PetSize;
import java.time.Instant;
import java.util.List;

public record PetResponseDTO(
        Long petId,
        String petName,
        String species,
        String description,
        Integer ageInMonths,
        PetSize size,
        List<PetPhotoResponseDTO> photos,
        Boolean isAvailableForAdoption,
        Instant createdAt
) {
}
