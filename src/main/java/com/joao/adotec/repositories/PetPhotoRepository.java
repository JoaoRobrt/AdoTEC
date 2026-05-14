package com.joao.adotec.repositories;

import com.joao.adotec.models.Pet;
import com.joao.adotec.models.PetPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetPhotoRepository extends JpaRepository<PetPhoto, Long> {
    List<PetPhoto> findByPetOrderByCreatedAtAsc(Pet pet);
    long countByPet(Pet pet);
}
