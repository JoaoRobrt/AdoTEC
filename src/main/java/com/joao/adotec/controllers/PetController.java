package com.joao.adotec.controllers;

import com.joao.adotec.dto.PetRequestDTO;
import com.joao.adotec.dto.PetResponseDTO;
import com.joao.adotec.services.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
public class PetController {

     private final PetService petService;

    @GetMapping
    public ResponseEntity<List<PetResponseDTO>> getAllAvailablePets() {
        List<PetResponseDTO> pets = petService.getAllAvailablePets();
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PetResponseDTO> getPetById(@PathVariable Long id) {
        PetResponseDTO pet = petService.getPetById(id);
        return ResponseEntity.ok(pet);
    }

    @PostMapping
    public ResponseEntity<PetResponseDTO> createPet(@RequestBody PetRequestDTO petDto) {
        PetResponseDTO createdPet = petService.createPet(petDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetResponseDTO> updatePet(@PathVariable Long id, @RequestBody PetRequestDTO petDto) {
        PetResponseDTO updatedPet = petService.updatePet(id, petDto);
        return ResponseEntity.ok(updatedPet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return ResponseEntity.noContent().build();
    }
}
