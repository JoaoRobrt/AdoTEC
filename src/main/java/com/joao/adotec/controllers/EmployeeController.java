package com.joao.adotec.controllers;

import com.joao.adotec.dto.CreateEmployeeRequest;
import com.joao.adotec.dto.UpdateEmployeeRequest;
import com.joao.adotec.dto.UserResponse;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.security.services.UserDetailsImpl;
import com.joao.adotec.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Employees", description = "Endpoints para gerenciamento de funcionários (CRUD - Apenas ADMIN)")
public class EmployeeController {

    private final UserService userService;

    @Operation(
            summary = "Listar funcionários",
            description = "Retorna todos os usuários que possuem a permissão ROLE_EMPLOYEE. **Requer permissão de ADMIN.**"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Funcionários listados com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado — requer ROLE_ADMIN")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getEmployees() {
        List<UserResponse> employees = userService.findEmployees();
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }

    @Operation(
            summary = "Buscar funcionário por ID",
            description = "Retorna os detalhes de um funcionário específico pelo ID. **Requer permissão de ADMIN.**"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Funcionário retornado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getEmployeeById(
            @Parameter(description = "ID do funcionário", example = "1")
            @PathVariable Long id) {
        UserResponse employee = userService.findEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", employee));
    }

    @Operation(
            summary = "Cadastrar funcionário",
            description = "Cria um novo perfil de usuário funcionário (ROLE_EMPLOYEE) ou administrador (ROLE_ADMIN). O e-mail informado deve ser único. **Requer permissão de ADMIN.**"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Funcionário cadastrado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "E-mail já está em uso")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {
        UserResponse created = userService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", created));
    }

    @Operation(
            summary = "Atualizar funcionário",
            description = "Atualiza o nome e o e-mail de um funcionário existente. **Requer permissão de ADMIN.**"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Funcionário atualizado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Funcionário não encontrado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "E-mail já está em uso")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateEmployee(
            @Parameter(description = "ID do funcionário", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeRequest request) {
        UserResponse updated = userService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updated));
    }

    @Operation(
            summary = "Ativar ou desativar funcionário",
            description = "Ativa ou desativa (soft delete) um funcionário. Um funcionário desativado não consegue logar no sistema. O administrador não pode desativar a si próprio. **Requer permissão de ADMIN.**"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status do funcionário alterado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Operação inválida (ex: tentar se autodesativar)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Funcionário não encontrado")
    })
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<ApiResponse<UserResponse>> toggleActive(
            @Parameter(description = "ID do funcionário", example = "1")
            @PathVariable Long id,
            Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UserResponse toggled = userService.toggleActive(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Employee status toggled successfully", toggled));
    }
}
