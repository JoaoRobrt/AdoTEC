package com.joao.adotec.mappers;

import com.joao.adotec.dto.PetPhotoResponseDTO;
import com.joao.adotec.models.PetPhoto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PetPhotoMapper {
    PetPhotoResponseDTO toDTO(PetPhoto petPhoto);
    List<PetPhotoResponseDTO> toDTO(List<PetPhoto> petPhotos);
}
