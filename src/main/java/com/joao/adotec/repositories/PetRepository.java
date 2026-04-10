package com.joao.adotec.repositories;

import com.joao.adotec.models.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    /** Public listing: only active (not deleted) AND available pets. */
    Page<Pet> findByIsAvailableForAdoptionTrueAndIsActiveTrue(Pageable pageable);

    /** Legacy alias kept for reference — now delegates to the above. */
    Page<Pet> findByIsAvailableForAdoptionTrue(Pageable pageable);
}
