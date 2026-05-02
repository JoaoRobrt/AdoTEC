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
}
