package com.joao.adotec.dto.commons;

import com.joao.adotec.dto.PetResponseDTO;
import java.util.List;

/**
 * Wrapper class to hold a list of PetResponseDTOs for caching purposes.
 * This ensures GenericJackson2JsonRedisSerializer can properly deserialize
 * the inner records without type erasure issues.
 */
public class PetsDestaqueCacheWrapper {
    private List<PetResponseDTO> pets;

    public PetsDestaqueCacheWrapper() {
    }

    public PetsDestaqueCacheWrapper(List<PetResponseDTO> pets) {
        this.pets = pets;
    }

    public List<PetResponseDTO> getPets() {
        return pets;
    }

    public void setPets(List<PetResponseDTO> pets) {
        this.pets = pets;
    }
}
