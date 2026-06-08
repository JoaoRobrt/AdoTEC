package com.joao.adotec.controllers;

import com.joao.adotec.dto.PetRequestDTO;
import com.joao.adotec.dto.PetResponseDTO;
import com.joao.adotec.dto.commons.PageMetaDTO;
import com.joao.adotec.dto.commons.PageResponseDTO;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.services.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Pets", description = "Endpoints para cadastro e consulta de pets disponíveis para adoção")
public class PetController {

    private final PetService petService;

    @Operation(
            summary = "Listar pets disponíveis",
            description = """
                    Retorna pets disponíveis para adoção com paginação e filtros opcionais. **Acesso público.**

                    ### Filtros
                    | Parâmetro  | Descrição                                  | Exemplo                  |
                    |------------|---------------------------------------------|--------------------------|
                    | `petSize`  | Filtra pelo porte do pet                    | `?petSize=SMALL`         |
                    | `species`  | Filtra pela espécie (ex: Cachorro, Gato)     | `?species=Cachorro`      |
                    | `minAge`   | Idade mínima em meses                        | `?minAge=6`              |
                    | `maxAge`   | Idade máxima em meses                        | `?maxAge=24`             |
                    | `gender`   | Filtra pelo sexo do pet                      | `?gender=FEMALE`         |
                    | `name`     | Busca parcial pelo nome do pet               | `?name=Rex`              |

                    ### Paginação
                    Padrão: `page=0`, `size=12`."""
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pets retornados com sucesso")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<PetResponseDTO>>> getAllAvailablePets(
            @Parameter(description = "Filtra pelo porte do pet. Valores: SMALL, MEDIUM, BIG", example = "MEDIUM")
            @RequestParam(required = false) com.joao.adotec.enums.PetSize petSize,
            @Parameter(description = "Filtra pela espécie (ex: Cachorro, Gato, Coelho)", example = "Cachorro")
            @RequestParam(required = false) String species,
            @Parameter(description = "Idade mínima em meses", example = "6")
            @RequestParam(required = false) Integer minAge,
            @Parameter(description = "Idade máxima em meses", example = "36")
            @RequestParam(required = false) Integer maxAge,
            @Parameter(description = "Filtra pelo sexo do pet. Valores: MALE, FEMALE", example = "FEMALE")
            @RequestParam(required = false) com.joao.adotec.enums.PetGender gender,
            @Parameter(description = "Busca parcial pelo nome do pet", example = "Rex")
            @RequestParam(required = false) String name,
            @Parameter(hidden = true)
            @PageableDefault(size = 12, page = 0) Pageable pageable) {
        Page<PetResponseDTO> petsPage = petService.getAllAvailablePets(petSize, species, minAge, maxAge, gender, name, pageable);
        PageResponseDTO<PetResponseDTO> pageResponse = new PageResponseDTO<>(
                petsPage.getContent(),
                PageMetaDTO.fromPage(petsPage)
        );
        return ResponseEntity.ok(ApiResponse.success("Pets retrieved successfully", pageResponse));
    }

    @Operation(
            summary = "Pets em destaque",
            description = """
                    Retorna os 4 pets mais recentes disponíveis para adoção (destaques da home page).
                    Resultado cacheado no Redis (TTL configurável em `cache.pets.destaque.ttl-minutes`). **Acesso público.**"""
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pets em destaque retornados com sucesso")
    })
    @GetMapping("/destaque")
    public ResponseEntity<ApiResponse<java.util.List<PetResponseDTO>>> getDestaquePets() {
        java.util.List<PetResponseDTO> pets = petService.getDestaquePets().getPets();
        return ResponseEntity.ok(ApiResponse.success("Featured pets retrieved successfully", pets));
    }

    @Operation(
            summary = "Buscar pet por ID",
            description = "Retorna informações detalhadas de um pet específico. Retorna 404 se o pet estiver inativo ou indisponível. **Acesso público.**"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pet retornado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet não encontrado ou inativo")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PetResponseDTO>> getPetById(
            @Parameter(description = "ID do pet", example = "1")
            @PathVariable Long id) {
        PetResponseDTO pet = petService.getPetById(id);
        return ResponseEntity.ok(ApiResponse.success("Pet retrieved successfully", pet));
    }

    @Operation(
            summary = "Cadastrar pet",
            description = "Registra um novo pet no sistema. O pet é criado como ativo e disponível para adoção. Requer ROLE_ADMIN ou ROLE_EMPLOYEE."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Pet cadastrado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Erro de validação"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetResponseDTO>> createPet(@Valid @RequestBody PetRequestDTO petDto) {
        PetResponseDTO createdPet = petService.createPet(petDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pet created successfully", createdPet));
    }

    @Operation(
            summary = "Atualizar pet",
            description = "Atualiza as informações de um pet existente. Requer ROLE_ADMIN ou ROLE_EMPLOYEE."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pet atualizado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Erro de validação"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<PetResponseDTO>> updatePet(
            @Parameter(description = "ID do pet", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PetRequestDTO petDto) {
        PetResponseDTO updatedPet = petService.updatePet(id, petDto);
        return ResponseEntity.ok(ApiResponse.success("Pet updated successfully", updatedPet));
    }

    @Operation(
            summary = "Excluir pet (soft delete)",
            description = """
                    Marca o pet como inativo (soft delete). O pet deixa de aparecer nas listagens públicas
                    e não pode mais receber novos agendamentos. Os agendamentos existentes são preservados
                    para consulta de histórico. Requer ROLE_ADMIN ou ROLE_EMPLOYEE."""
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Pet excluído com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet não encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<Void> deletePet(
            @Parameter(description = "ID do pet", example = "1")
            @PathVariable Long id) {
        petService.deletePet(id);
        return ResponseEntity.noContent().build();
    }
}
