package com.joao.adotec.dto;

import com.joao.adotec.enums.PetSize;
import com.joao.adotec.models.Pet;
import java.time.Instant;

public record PetResponseDTO(
        Long petId,
        String petName,
        String description,
        Integer ageInMonths,
        PetSize size,
        String photoUrl,
        Boolean isAvailableForAdoption,
        Instant createdAt
) {
    public PetResponseDTO(Pet pet) {
        this(
                pet.getPetId(),
                pet.getPetName(),
                pet.getDescription(),
                pet.getAgeInMonths(),
                pet.getSize(),
                pet.getPhotoUrl(),
                pet.getIsAvailableForAdoption(),
                pet.getCreatedAt()
        );
    }
}
