package com.joao.adotec.mappers;

import com.joao.adotec.dto.PetRequestDTO;
import com.joao.adotec.dto.PetResponseDTO;
import com.joao.adotec.models.Pet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PetMapper {

    PetMapper INSTANCE = Mappers.getMapper(PetMapper.class);

    PetResponseDTO toDTO(Pet pet);

    @Mapping(target = "petId", ignore = true)
    @Mapping(target = "isAvailableForAdoption", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Pet toEntity(PetRequestDTO petRequestDTO);

    List<PetResponseDTO> toDTOList(List<Pet> pets);

    @Mapping(target = "petId", ignore = true)
    @Mapping(target = "isAvailableForAdoption", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updatePetFromDto(PetRequestDTO petRequestDTO, @MappingTarget Pet pet);
}
