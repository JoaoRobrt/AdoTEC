package com.joao.adotec.dto;

import com.joao.adotec.enums.PetSize;
import com.joao.adotec.enums.PetGender;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(description = "Representa um pet cadastrado no sistema")
public record PetResponseDTO(
        @Schema(description = "ID único do pet", example = "1")
        Long petId,
        
        @Schema(description = "Nome do pet", example = "Rex")
        String petName,
        
        @Schema(description = "Espécie do pet (ex: Cachorro, Gato)", example = "Cachorro")
        String species,
        
        @Schema(description = "Descrição das características, temperamento e cuidados", example = "Rex é um vira-lata grandalhão, protetor e carinhoso.")
        String description,
        
        @Schema(description = "Idade aproximada em meses", example = "36")
        Integer ageInMonths,
        
        @Schema(description = "Porte físico do pet", example = "BIG")
        PetSize size,
        
        @Schema(description = "Sexo/Gênero do pet", example = "MALE")
        PetGender gender,
        
        @Schema(description = "Lista de fotos vinculadas ao pet")
        List<PetPhotoResponseDTO> photos,
        
        @Schema(description = "Indica se o pet está disponível para novas visitas de adoção", example = "true")
        Boolean isAvailableForAdoption,
        
        @Schema(description = "Data e hora do cadastro do pet no sistema", example = "2026-06-08T18:54:00Z")
        Instant createdAt
) {
}
