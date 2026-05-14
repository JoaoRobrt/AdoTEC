package com.joao.adotec.controllers;

import com.joao.adotec.dto.PetPhotoResponseDTO;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.mappers.PetPhotoMapper;
import com.joao.adotec.models.PetPhoto;
import com.joao.adotec.services.PetPhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/pets/{petId}/photos")
@RequiredArgsConstructor
@Tag(name = "Pet Photos", description = "Endpoints for managing pet photos via Cloudinary")
public class PetPhotoController {

    private final PetPhotoService petPhotoService;
    private final PetPhotoMapper petPhotoMapper;

    @Operation(summary = "Upload a pet photo", description = "Uploads a photo for a specific pet. Requires ADMIN or EMPLOYEE role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Photo uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error (invalid format, size exceeded)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet not found or inactive"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Maximum number of photos exceeded")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetPhotoResponseDTO>> uploadPhoto(
            @PathVariable Long petId,
            @RequestParam("file") MultipartFile file) {

        PetPhoto petPhoto = petPhotoService.uploadPhoto(petId, file);
        PetPhotoResponseDTO responseData = petPhotoMapper.toDTO(petPhoto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Photo uploaded successfully", responseData));
    }

    @Operation(summary = "Get all photos of a pet", description = "Retrieves all photos of a specific pet. Publicly accessible.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Photos retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet not found or inactive")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PetPhotoResponseDTO>>> getPhotosByPet(@PathVariable Long petId) {
        List<PetPhoto> photos = petPhotoService.getPhotosByPet(petId);
        List<PetPhotoResponseDTO> responseData = petPhotoMapper.toDTO(photos);
        return ResponseEntity.ok(ApiResponse.success("Photos retrieved successfully", responseData));
    }

    @Operation(summary = "Delete a pet photo", description = "Deletes a specific photo of a pet. Requires ADMIN or EMPLOYEE role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Photo deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet or photo not found")
    })
    @DeleteMapping("/{photoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @PathVariable Long petId,
            @PathVariable Long photoId) {

        petPhotoService.deletePhoto(petId, photoId);
        return ResponseEntity.ok(ApiResponse.success("Photo deleted successfully", null));
    }

    @Operation(summary = "Set a primary photo", description = "Sets a specific photo as the primary photo for a pet. Requires ADMIN or EMPLOYEE role.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Primary photo updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet or photo not found")
    })
    @PatchMapping("/{photoId}/primary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetPhotoResponseDTO>> setPrimaryPhoto(
            @PathVariable Long petId,
            @PathVariable Long photoId) {

        PetPhoto petPhoto = petPhotoService.setPrimaryPhoto(petId, photoId);
        PetPhotoResponseDTO responseData = petPhotoMapper.toDTO(petPhoto);

        return ResponseEntity.ok(ApiResponse.success("Primary photo updated successfully", responseData));
    }
}
