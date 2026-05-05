package com.joao.adotec.services;

import com.joao.adotec.exceptions.domain.ResourceNotFoundException;
import com.joao.adotec.mappers.PetMapper;
import com.joao.adotec.repositories.PetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * Unit tests for PetService.
 *
 * These tests run without Spring context, using plain Mockito.
 * Focus: verify that the custom exception handling works correctly (DT-002 fix).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PetService — Unit Tests")
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private PetMapper petMapper;

    @InjectMocks
    private PetService petService;

    @Test
    @DisplayName("getPetById → throws ResourceNotFoundException when pet does not exist")
    void getPetById_whenPetNotFound_shouldThrowResourceNotFoundException() {
        Long nonExistentId = 999L;
        given(petRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> petService.getPetById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pet")
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("getPetById → ResourceNotFoundException maps to HTTP 404")
    void getPetById_whenPetNotFound_exceptionShouldHave404Status() {
        Long nonExistentId = 999L;
        given(petRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> petService.getPetById(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(ex -> {
                    ResourceNotFoundException rnfe = (ResourceNotFoundException) ex;
                    assertThat(rnfe.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("getPetById → throws ResourceNotFoundException if pet is not available for adoption")
    void getPetById_whenPetNotAvailable_shouldThrowResourceNotFoundException() {
        Long id = 1L;
        com.joao.adotec.models.Pet pet = new com.joao.adotec.models.Pet();
        pet.setPetId(id);
        pet.setIsActive(true);
        pet.setIsAvailableForAdoption(false);

        given(petRepository.findById(id)).willReturn(Optional.of(pet));

        assertThatThrownBy(() -> petService.getPetById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pet")
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("getPetById → throws ResourceNotFoundException if pet is inactive")
    void getPetById_whenPetInactive_shouldThrowResourceNotFoundException() {
        Long id = 1L;
        com.joao.adotec.models.Pet pet = new com.joao.adotec.models.Pet();
        pet.setPetId(id);
        pet.setIsActive(false);
        pet.setIsAvailableForAdoption(true);

        given(petRepository.findById(id)).willReturn(Optional.of(pet));

        assertThatThrownBy(() -> petService.getPetById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Pet")
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("getPetById → returns DTO when pet is active and available")
    void getPetById_whenPetValid_shouldReturnDTO() {
        Long id = 1L;
        com.joao.adotec.models.Pet pet = new com.joao.adotec.models.Pet();
        pet.setPetId(id);
        pet.setIsActive(true);
        pet.setIsAvailableForAdoption(true);
        pet.setSpecies("Dog");

        com.joao.adotec.dto.PetResponseDTO dto = new com.joao.adotec.dto.PetResponseDTO(pet);
        
        given(petRepository.findById(id)).willReturn(Optional.of(pet));
        given(petMapper.toDTO(pet)).willReturn(dto);

        com.joao.adotec.dto.PetResponseDTO result = petService.getPetById(id);
        
        assertThat(result).isNotNull();
        assertThat(result.species()).isEqualTo("Dog");
    }

    @Test
    @DisplayName("updatePet → throws ResourceNotFoundException when pet does not exist")
    void updatePet_whenPetNotFound_shouldThrowResourceNotFoundException() {
        Long nonExistentId = 42L;
        given(petRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> petService.updatePet(nonExistentId, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("deletePet → throws ResourceNotFoundException when pet does not exist")
    void deletePet_whenPetNotFound_shouldThrowResourceNotFoundException() {

        Long nonExistentId = 7L;
        given(petRepository.findById(nonExistentId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> petService.deletePet(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("7");
    }

    @Test
    @DisplayName("getAllAvailablePets → filters by size")
    void getAllAvailablePets_filtersBySize() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.Pageable.unpaged();
        com.joao.adotec.enums.PetSize size = com.joao.adotec.enums.PetSize.SMALL;
        
        given(petRepository.findAvailablePetsWithFilters(size, null, pageable))
                .willReturn(org.springframework.data.domain.Page.empty());

        petService.getAllAvailablePets(size, null, pageable);

        org.mockito.Mockito.verify(petRepository).findAvailablePetsWithFilters(size, null, pageable);
    }

    @Test
    @DisplayName("getAllAvailablePets → filters by name")
    void getAllAvailablePets_filtersByName() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.Pageable.unpaged();
        String name = "Rex";
        
        given(petRepository.findAvailablePetsWithFilters(null, name, pageable))
                .willReturn(org.springframework.data.domain.Page.empty());

        petService.getAllAvailablePets(null, name, pageable);

        org.mockito.Mockito.verify(petRepository).findAvailablePetsWithFilters(null, name, pageable);
    }

    @Test
    @DisplayName("getAllAvailablePets → filters by size and name")
    void getAllAvailablePets_filtersByCombined() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.Pageable.unpaged();
        com.joao.adotec.enums.PetSize size = com.joao.adotec.enums.PetSize.SMALL;
        String name = "Rex";
        
        given(petRepository.findAvailablePetsWithFilters(size, name, pageable))
                .willReturn(org.springframework.data.domain.Page.empty());

        petService.getAllAvailablePets(size, name, pageable);

        org.mockito.Mockito.verify(petRepository).findAvailablePetsWithFilters(size, name, pageable);
    }
}
