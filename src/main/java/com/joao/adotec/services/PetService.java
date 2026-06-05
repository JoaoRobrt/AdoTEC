package com.joao.adotec.services;

import com.joao.adotec.dto.PetRequestDTO;
import com.joao.adotec.dto.PetResponseDTO;
import com.joao.adotec.exceptions.domain.ResourceNotFoundException;
import com.joao.adotec.mappers.PetMapper;
import com.joao.adotec.models.Pet;
import com.joao.adotec.repositories.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final PetMapper petMapper;

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> getAllAvailablePets(
            com.joao.adotec.enums.PetSize size,
            String species,
            Integer minAge,
            Integer maxAge,
            com.joao.adotec.enums.PetGender gender,
            String name,
            Pageable pageable) {
        // Filter: isAvailableForAdoption=true AND isActive=true (not soft-deleted), plus size, species, age, gender, and name
        return petRepository.findAvailablePetsWithFilters(size, species, minAge, maxAge, gender, name, pageable).map(petMapper::toDTO);
    }

    /**
     * Retorna os 4 pets mais recentes disponíveis para adoção (destaque da home).
     * Resultado cacheado no Redis usando um Wrapper para preservar a tipagem JSON.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "petsDestaque", key = "'lista'")
    public com.joao.adotec.dto.commons.PetsDestaqueCacheWrapper getDestaquePets() {
        List<PetResponseDTO> list = petRepository.findTop4ByIsAvailableForAdoptionTrueAndIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(petMapper::toDTO)
                .toList();
        return new com.joao.adotec.dto.commons.PetsDestaqueCacheWrapper(list);
    }

    @Transactional(readOnly = true)
    public PetResponseDTO getPetById(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", id));
        
        if (Boolean.FALSE.equals(pet.getIsActive()) || Boolean.FALSE.equals(pet.getIsAvailableForAdoption())) {
            throw new ResourceNotFoundException("Pet", id);
        }
        
        return petMapper.toDTO(pet);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "petsDestaque", allEntries = true),
            @CacheEvict(value = "dashboardMetrics", allEntries = true)
    })
    public PetResponseDTO createPet(PetRequestDTO petDto) {
        Pet pet = petMapper.toEntity(petDto);
        pet.setIsAvailableForAdoption(true);

        pet = petRepository.save(pet);
        return petMapper.toDTO(pet);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "petsDestaque", allEntries = true),
            @CacheEvict(value = "dashboardMetrics", allEntries = true)
    })
    public PetResponseDTO updatePet(Long id, PetRequestDTO petDto) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", id));

        petMapper.updatePetFromDto(petDto, pet);

        pet = petRepository.save(pet);
        return petMapper.toDTO(pet);
    }

    /**
     * Soft-deletes a pet by setting {@code isActive = false}.
     * All linked appointments are preserved in the database so the Admin can
     * still view the full adoption history (RF15). The pet will no longer
     * appear in any public listing or be bookable for new appointments.
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "petsDestaque", allEntries = true),
            @CacheEvict(value = "dashboardMetrics", allEntries = true)
    })
    public void deletePet(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", id));

        // Soft delete: flag the record as inactive instead of physically removing it.
        pet.setIsActive(false);
        petRepository.save(pet);
    }
}

