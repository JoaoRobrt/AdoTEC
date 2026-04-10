package com.joao.adotec.dto;

import com.joao.adotec.enums.PetSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PetRequestDTO(

        @NotBlank(message = "Pet name is required")
        String petName,

        String description,

        Integer ageInMonths,

        @NotNull(message = "Pet size is required")
        PetSize size,

        String photoUrl
) {
}
