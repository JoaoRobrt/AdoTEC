package com.joao.adotec.services;

import com.joao.adotec.dto.PetRequestDTO;
import com.joao.adotec.dto.PetResponseDTO;
import com.joao.adotec.exceptions.domain.ResourceNotFoundException;
import com.joao.adotec.mappers.PetMapper;
import com.joao.adotec.models.Pet;
import com.joao.adotec.repositories.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final PetMapper petMapper;

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> getAllAvailablePets(Pageable pageable) {
        return petRepository.findByIsAvailableForAdoptionTrue(pageable).map(petMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public PetResponseDTO getPetById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", id));
        return petMapper.toDTO(pet);
    }

    @Transactional
    public PetResponseDTO createPet(PetRequestDTO petDto) {
        Pet pet = petMapper.toEntity(petDto);
        pet.setIsAvailableForAdoption(true);

        pet = petRepository.save(pet);
        return petMapper.toDTO(pet);
    }

    @Transactional
    public PetResponseDTO updatePet(Long id, PetRequestDTO petDto) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", id));

        petMapper.updatePetFromDto(petDto, pet);

        pet = petRepository.save(pet);
        return petMapper.toDTO(pet);
    }

    @Transactional
    public PetResponseDTO deletePet(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", id));
        petRepository.delete(pet);
        return petMapper.toDTO(pet);
    }
}
