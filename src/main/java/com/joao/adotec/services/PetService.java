package com.joao.adotec.services;

import com.joao.adotec.dto.PetRequestDTO;
import com.joao.adotec.dto.PetResponseDTO;
import com.joao.adotec.mappers.PetMapper;
import com.joao.adotec.models.Pet;
import com.joao.adotec.repositories.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final PetMapper petMapper;

    @Transactional(readOnly = true)
    public List<PetResponseDTO> getAllAvailablePets() {
        return petMapper.toDTOList(petRepository.findByIsAvailableForAdoptionTrue());
    }

    @Transactional(readOnly = true)
    public PetResponseDTO getPetById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
        return petMapper.toDTO(pet);
    }

    @Transactional
    public PetResponseDTO createPet(PetRequestDTO petDto) {
        Pet pet = petMapper.toEntity(petDto);
        pet.setIsAvailableForAdoption(true); // default value
        
        pet = petRepository.save(pet);
        return petMapper.toDTO(pet);
    }

    @Transactional
    public PetResponseDTO updatePet(Long id, PetRequestDTO petDto) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
                
        petMapper.updatePetFromDto(petDto, pet);
        
        pet = petRepository.save(pet);
        return petMapper.toDTO(pet);
    }

    @Transactional
    public PetResponseDTO deletePet(Long id) {
       Pet pet = petRepository.findById(id).orElseThrow(() -> new RuntimeException("Pet not found with id: " + id));
       petRepository.delete(pet);
       return petMapper.toDTO(pet);
    }
}
