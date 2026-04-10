package com.joao.adotec.repositories;

import com.joao.adotec.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    Page<Pet> findByIsAvailableForAdoptionTrue(Pageable pageable);
}
