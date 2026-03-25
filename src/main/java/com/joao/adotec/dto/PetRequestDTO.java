package com.joao.adotec.dto;

import com.joao.adotec.enums.PetSize;

public record PetRequestDTO(
        String petName,
        String description,
        Integer ageInMonths,
        PetSize size,
        String photoUrl
) {
}
