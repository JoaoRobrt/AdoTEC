package com.joao.adotec.controllers;

import com.joao.adotec.dto.PetRequestDTO;
import com.joao.adotec.dto.PetResponseDTO;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.services.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PetResponseDTO>>> getAllAvailablePets() {
        List<PetResponseDTO> pets = petService.getAllAvailablePets();
        return ResponseEntity.ok(ApiResponse.success("Pets retrieved successfully", pets));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PetResponseDTO>> getPetById(@PathVariable Long id) {
        PetResponseDTO pet = petService.getPetById(id);
        return ResponseEntity.ok(ApiResponse.success("Pet retrieved successfully", pet));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetResponseDTO>> createPet(@Valid @RequestBody PetRequestDTO petDto) {
        PetResponseDTO createdPet = petService.createPet(petDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Pet created successfully", createdPet));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetResponseDTO>> updatePet(@PathVariable Long id, @Valid @RequestBody PetRequestDTO petDto) {
        PetResponseDTO updatedPet = petService.updatePet(id, petDto);
        return ResponseEntity.ok(ApiResponse.success("Pet updated successfully", updatedPet));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetResponseDTO>> deletePet(@PathVariable Long id) {
        PetResponseDTO deletedPet = petService.deletePet(id);
        return ResponseEntity.ok(ApiResponse.success("Pet deleted successfully", deletedPet));
    }
}

