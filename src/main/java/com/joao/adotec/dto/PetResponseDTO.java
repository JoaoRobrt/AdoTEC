package com.joao.adotec.dto;

import com.joao.adotec.enums.PetSize;
import com.joao.adotec.enums.PetGender;
import java.time.Instant;
import java.util.List;

public record PetResponseDTO(
        Long petId,
        String petName,
        String species,
        String description,
        Integer ageInMonths,
        PetSize size,
        PetGender gender,
        List<PetPhotoResponseDTO> photos,
        Boolean isAvailableForAdoption,
        Instant createdAt
) {
}
