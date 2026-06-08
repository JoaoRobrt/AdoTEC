package com.joao.adotec.controllers;

import com.joao.adotec.dto.PetPhotoResponseDTO;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.mappers.PetPhotoMapper;
import com.joao.adotec.models.PetPhoto;
import com.joao.adotec.services.PetPhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Pet Photos", description = "Endpoints para gerenciamento de fotos dos pets integrados ao Cloudinary")
public class PetPhotoController {

    private final PetPhotoService petPhotoService;
    private final PetPhotoMapper petPhotoMapper;

    @Operation(
            summary = "Fazer upload de foto",
            description = "Envia uma imagem para o Cloudinary e associa ao pet correspondente. O sistema permite no máximo 5 fotos por pet. **Requer permissão de ADMIN ou EMPLOYEE.**"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Upload concluído com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Erro de validação (formato de arquivo inválido ou tamanho excedido)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet não encontrado ou inativo"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Limite máximo de 5 fotos do pet atingido")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetPhotoResponseDTO>> uploadPhoto(
            @Parameter(description = "ID do pet", example = "1")
            @PathVariable Long petId,
            @Parameter(description = "Arquivo da imagem (multipart/form-data)")
            @RequestParam("file") MultipartFile file) {

        PetPhoto petPhoto = petPhotoService.uploadPhoto(petId, file);
        PetPhotoResponseDTO responseData = petPhotoMapper.toDTO(petPhoto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Photo uploaded successfully", responseData));
    }

    @Operation(
            summary = "Listar todas as fotos de um pet",
            description = "Retorna todas as fotos cadastradas para o pet especificado. **Acesso público.**"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fotos listadas com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet não encontrado ou inativo")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<PetPhotoResponseDTO>>> getPhotosByPet(
            @Parameter(description = "ID do pet", example = "1")
            @PathVariable Long petId) {
        List<PetPhoto> photos = petPhotoService.getPhotosByPet(petId);
        List<PetPhotoResponseDTO> responseData = petPhotoMapper.toDTO(photos);
        return ResponseEntity.ok(ApiResponse.success("Photos retrieved successfully", responseData));
    }

    @Operation(
            summary = "Remover foto de um pet",
            description = "Remove o registro da foto do banco de dados e exclui o arquivo hospedado no Cloudinary. **Requer permissão de ADMIN ou EMPLOYEE.**"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Foto removida com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet ou foto não encontrada")
    })
    @DeleteMapping("/{photoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @Parameter(description = "ID do pet", example = "1")
            @PathVariable Long petId,
            @Parameter(description = "ID da foto a ser excluída", example = "2")
            @PathVariable Long photoId) {

        petPhotoService.deletePhoto(petId, photoId);
        return ResponseEntity.ok(ApiResponse.success("Photo deleted successfully", null));
    }

    @Operation(
            summary = "Definir foto principal do pet",
            description = "Define a foto especificada como principal para o pet. Todas as outras fotos deste pet são desmarcadas como principal automaticamente. **Requer permissão de ADMIN ou EMPLOYEE.**"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Foto principal definida com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet ou foto não encontrada")
    })
    @PatchMapping("/{photoId}/primary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetPhotoResponseDTO>> setPrimaryPhoto(
            @Parameter(description = "ID do pet", example = "1")
            @PathVariable Long petId,
            @Parameter(description = "ID da foto a ser definida como principal", example = "2")
            @PathVariable Long photoId) {

        PetPhoto petPhoto = petPhotoService.setPrimaryPhoto(petId, photoId);
        PetPhotoResponseDTO responseData = petPhotoMapper.toDTO(petPhoto);

        return ResponseEntity.ok(ApiResponse.success("Primary photo updated successfully", responseData));
    }
}
