package com.joao.adotec.dto;

import com.joao.adotec.enums.PetSize;
import com.joao.adotec.enums.PetGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PetRequestDTO(

        @NotBlank(message = "Pet name is required")
        String petName,

        @NotBlank(message = "Species is required")
        String species,

        String description,

        Integer ageInMonths,

        @NotNull(message = "Pet size is required")
        PetSize size,

        @NotNull(message = "Pet gender is required")
        PetGender gender
) {
}
