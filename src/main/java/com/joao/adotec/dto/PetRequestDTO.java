package com.joao.adotec.dto;

import com.joao.adotec.enums.PetSize;
import com.joao.adotec.enums.PetGender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados de entrada para criação ou atualização de um pet")
public record PetRequestDTO(

        @NotBlank(message = "Pet name is required")
        @Schema(description = "Nome do pet", example = "Rex")
        String petName,

        @NotBlank(message = "Species is required")
        @Schema(description = "Espécie do pet", example = "Cachorro")
        String species,

        @Schema(description = "Descrição detalhada sobre a história, comportamento ou necessidades do pet", example = "Rex é um vira-lata grandalhão, super protetor e carinhoso.")
        String description,

        @Schema(description = "Idade estimada em meses", example = "36")
        Integer ageInMonths,

        @NotNull(message = "Pet size is required")
        @Schema(description = "Porte físico do pet", example = "BIG")
        PetSize size,

        @NotNull(message = "Pet gender is required")
        @Schema(description = "Sexo/Gênero do pet", example = "MALE")
        PetGender gender
) {
}
