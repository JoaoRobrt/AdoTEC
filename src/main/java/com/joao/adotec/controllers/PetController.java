package com.joao.adotec.controllers;

import com.joao.adotec.dto.PetRequestDTO;
import com.joao.adotec.dto.PetResponseDTO;
import com.joao.adotec.dto.commons.PageMetaDTO;
import com.joao.adotec.dto.commons.PageResponseDTO;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.services.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Endpoints for managing pets in the adoption center")
public class PetController {

    private final PetService petService;

    @Operation(summary = "Get all available pets", description = "Retrieves a list of all pets that are currently available for adoption. Publicly accessible.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved the list of pets")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<PetResponseDTO>>> getAllAvailablePets(
            @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<PetResponseDTO> petsPage = petService.getAllAvailablePets(pageable);
        PageResponseDTO<PetResponseDTO> pageResponse = new PageResponseDTO<>(
                petsPage.getContent(),
                PageMetaDTO.fromPage(petsPage)
        );
        return ResponseEntity.ok(ApiResponse.success("Pets retrieved successfully", pageResponse));
    }

    @Operation(summary = "Get pet by ID", description = "Retrieves detailed information about a specific pet by its ID. Publicly accessible.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved the pet"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PetResponseDTO>> getPetById(@PathVariable Long id) {
        PetResponseDTO pet = petService.getPetById(id);
        return ResponseEntity.ok(ApiResponse.success("Pet retrieved successfully", pet));
    }

    @Operation(summary = "Create pet", description = "Registers a new pet in the system. Requires ADMIN or EMPLOYEE role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Pet created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error in request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetResponseDTO>> createPet(@Valid @RequestBody PetRequestDTO petDto) {
        PetResponseDTO createdPet = petService.createPet(petDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pet created successfully", createdPet));
    }

    @Operation(summary = "Update pet", description = "Updates an existing pet's information. Requires ADMIN or EMPLOYEE role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pet updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error in request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetResponseDTO>> updatePet(@PathVariable Long id,
            @Valid @RequestBody PetRequestDTO petDto) {
        PetResponseDTO updatedPet = petService.updatePet(id, petDto);
        return ResponseEntity.ok(ApiResponse.success("Pet updated successfully", updatedPet));
    }

    @Operation(summary = "Delete pet", description = "Deletes a pet from the system by ID (or performs a logical deletion depending on implementation). Requires ADMIN or EMPLOYEE role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pet deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetResponseDTO>> deletePet(@PathVariable Long id) {
        PetResponseDTO deletedPet = petService.deletePet(id);
        return ResponseEntity.ok(ApiResponse.success("Pet deleted successfully", deletedPet));
    }
}
