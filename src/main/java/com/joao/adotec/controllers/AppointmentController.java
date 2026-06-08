package com.joao.adotec.controllers;

import com.joao.adotec.dto.AppointmentRequestDTO;
import com.joao.adotec.dto.AppointmentResponseDTO;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.mappers.AppointmentMapper;
import com.joao.adotec.models.Appointment;
import com.joao.adotec.security.services.UserDetailsImpl;
import com.joao.adotec.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import com.joao.adotec.dto.commons.PageMetaDTO;
import com.joao.adotec.dto.commons.PageResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.joao.adotec.enums.AppointmentStatus;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Endpoints para agendamento e gestão de visitas de adoção")
public class AppointmentController {

        private final AppointmentService appointmentService;
        private final AppointmentMapper appointmentMapper;

        @Operation(
                summary = "Agendar visita",
                description = """
                        Permite que um Adotante agende uma visita para um pet em um horário específico.

                        O `timeSlotId` deve estar no formato `YYYY-MM-DD_HH:MM` (ex: `2026-06-10_09:00`).
                        O sistema valida: data futura, horário configurado, capacidade do slot e duplicidade."""
        )
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Agendamento criado com sucesso"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Erro de validação no payload"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado — requer ROLE_ADOPTER"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pet ou TimeSlot não encontrado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Violação de regra de negócio (slot cheio ou duplicidade)")
        })
        @PostMapping
        @PreAuthorize("hasRole('ADOPTER')")
        public ResponseEntity<ApiResponse<AppointmentResponseDTO>> createAppointment(
                        @Valid @RequestBody AppointmentRequestDTO request,
                        Authentication authentication) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Long adopterId = userDetails.getId();

                Appointment appointment = appointmentService.createAppointment(adopterId, request.getPetId(),
                                request.getTimeSlotId());
                AppointmentResponseDTO responseData = appointmentMapper.toDTO(appointment);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Appointment scheduled successfully", responseData));
        }

        @Operation(
                summary = "Buscar agendamento por ID",
                description = """
                        Retorna um agendamento específico pelo seu ID.
                        - ADMIN e EMPLOYEE podem ver qualquer agendamento.
                        - ADOPTER só pode ver seus próprios agendamentos."""
        )
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Agendamento retornado com sucesso"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('EMPLOYEE') or hasRole('ADMIN') or hasRole('ADOPTER')")
        public ResponseEntity<ApiResponse<AppointmentResponseDTO>> getAppointmentById(
                        @Parameter(description = "ID do agendamento", example = "1")
                        @PathVariable Long id,
                        Authentication authentication) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Appointment appointment = appointmentService.getAppointmentById(id, userDetails);
                AppointmentResponseDTO responseData = appointmentMapper.toDTO(appointment);
                return ResponseEntity.ok(ApiResponse.success("Appointment retrieved successfully", responseData));
        }

        @Operation(
                summary = "Atribuir funcionário a um agendamento",
                description = "Permite que um ADMIN atribua um funcionário para acompanhar uma visita agendada."
        )
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Funcionário atribuído com sucesso"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado — requer ROLE_ADMIN"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Agendamento ou funcionário não encontrado")
        })
        @PatchMapping("/{id}/assign/{employeeId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<AppointmentResponseDTO>> assignEmployee(
                        @Parameter(description = "ID do agendamento", example = "1")
                        @PathVariable Long id,
                        @Parameter(description = "ID do funcionário a ser atribuído", example = "2")
                        @PathVariable Long employeeId) {

                Appointment appointment = appointmentService.assignEmployee(id, employeeId);
                AppointmentResponseDTO responseData = appointmentMapper.toDTO(appointment);

                return ResponseEntity.ok(ApiResponse.success("Employee assigned successfully", responseData));
        }

        @Operation(
                summary = "Meus agendamentos",
                description = """
                        Retorna os agendamentos do usuário autenticado com paginação, filtros e ordenação.

                        - **ADOPTER**: vê os agendamentos que ele próprio criou.
                        - **EMPLOYEE**: vê os agendamentos atribuídos a ele.

                        ### Filtros
                        | Parâmetro      | Descrição                                      | Exemplo               |
                        |----------------|-------------------------------------------------|-----------------------|
                        | `status`       | Filtra pelo status do agendamento               | `?status=PENDING`     |
                        | `showCanceled` | Inclui cancelados no resultado (padrão: `true`)  | `?showCanceled=false` |

                        ### Ordenação
                        Use `sort=campo,DIREÇÃO`. Campos suportados: `createdAt`, `appointmentDate`, `startTime`, `petName`.
                        O alias `petName` é convertido internamente para `pet.petName`.

                        ### Exemplos
                        - `GET /appointments/me?status=PENDING&sort=appointmentDate,ASC`
                        - `GET /appointments/me?page=1&size=5&sort=createdAt,DESC`"""
        )
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Agendamentos retornados com sucesso"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado — requer ROLE_ADOPTER ou ROLE_EMPLOYEE")
        })
        @GetMapping("/me")
        @PreAuthorize("hasRole('ADOPTER') or hasRole('EMPLOYEE')")
        public ResponseEntity<ApiResponse<PageResponseDTO<AppointmentResponseDTO>>> getMyAppointments(
                        Authentication authentication,
                        @Parameter(description = "Filtra pelo status do agendamento", example = "PENDING")
                        @RequestParam(required = false) AppointmentStatus status,
                        @Parameter(description = "Inclui agendamentos cancelados no resultado. Padrão: true", example = "true")
                        @RequestParam(defaultValue = "true") Boolean showCanceled,
                        @Parameter(hidden = true)
                        @PageableDefault(size = 10, page = 0) Pageable pageable) {

                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                Long userId = userDetails.getId();

                boolean isEmployee = authentication.getAuthorities().stream()
                                .anyMatch(auth -> auth.getAuthority().equals("ROLE_EMPLOYEE"));

                Page<Appointment> appointmentsPage;
                if (isEmployee) {
                        appointmentsPage = appointmentService.getAppointmentsByEmployee(userId, status, showCanceled, pageable);
                } else {
                        appointmentsPage = appointmentService.getAppointmentsByAdopter(userId, status, showCanceled, pageable);
                }

                Page<AppointmentResponseDTO> mappedPage = appointmentsPage.map(appointmentMapper::toDTO);
                PageResponseDTO<AppointmentResponseDTO> pageResponse = new PageResponseDTO<>(
                                mappedPage.getContent(),
                                PageMetaDTO.fromPage(mappedPage)
                );

                return ResponseEntity.ok(ApiResponse.success("Appointments fetched successfully", pageResponse));
        }

        @Operation(
                summary = "Listar todos os agendamentos",
                description = """
                        Retorna todos os agendamentos do sistema com paginação, filtros e ordenação. **Requer ROLE_ADMIN.**

                        ### Filtros
                        | Parâmetro      | Descrição                                                 | Exemplo                |
                        |----------------|----------------------------------------------------------|------------------------|
                        | `status`       | Filtra pelo status do agendamento                         | `?status=PENDING`      |
                        | `employeeId`   | Filtra agendamentos atribuídos a um funcionário específico | `?employeeId=3`        |
                        | `unassigned`   | Filtra apenas agendamentos sem funcionário atribuído       | `?unassigned=true`     |
                        | `showCanceled` | Inclui cancelados no resultado (padrão: `true`)            | `?showCanceled=false`  |

                        ### Ordenação
                        Use `sort=campo,DIREÇÃO`. Campos suportados: `createdAt`, `appointmentDate`, `startTime`, `petName`.
                        O alias `petName` é convertido internamente para `pet.petName`.

                        ### Exemplos
                        - `GET /appointments?status=PENDING&sort=appointmentDate,ASC`
                        - `GET /appointments?employeeId=3&page=0&size=10`
                        - `GET /appointments?unassigned=true&sort=createdAt,DESC`
                        - `GET /appointments?page=1&size=10`"""
        )
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Todos os agendamentos retornados com sucesso"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado — requer ROLE_ADMIN")
        })
        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<PageResponseDTO<AppointmentResponseDTO>>> getAllAppointments(
                        @Parameter(description = "Filtra pelo status do agendamento. Valores: PENDING, CONFIRMED, CANCELED, COMPLETED", example = "PENDING")
                        @RequestParam(required = false) AppointmentStatus status,
                        @Parameter(description = "Filtra agendamentos atribuídos a um funcionário específico pelo seu ID", example = "3")
                        @RequestParam(required = false) Long employeeId,
                        @Parameter(description = "Filtra apenas agendamentos sem funcionário atribuído (não atribuídos)", example = "true")
                        @RequestParam(required = false) Boolean unassigned,
                        @Parameter(description = "Inclui agendamentos cancelados no resultado. Padrão: true", example = "true")
                        @RequestParam(defaultValue = "true") Boolean showCanceled,
                        @Parameter(hidden = true)
                        @PageableDefault(size = 10, page = 0) Pageable pageable) {
                Page<Appointment> appointmentsPage = appointmentService.getAllAppointments(status, employeeId, unassigned, showCanceled, pageable);
                Page<AppointmentResponseDTO> mappedPage = appointmentsPage.map(appointmentMapper::toDTO);

                PageResponseDTO<AppointmentResponseDTO> pageResponse = new PageResponseDTO<>(
                                mappedPage.getContent(),
                                PageMetaDTO.fromPage(mappedPage)
                );

                return ResponseEntity.ok(ApiResponse.success("All appointments fetched successfully", pageResponse));
        }

        @Operation(
                summary = "Registrar resultado de visita",
                description = """
                        Registra o resultado final (APPROVED ou REJECTED) de uma visita de adoção.
                        Se APPROVED, o pet é marcado como indisponível para adoção.
                        O agendamento é movido para o status COMPLETED.
                        Requer ROLE_ADMIN ou ROLE_EMPLOYEE."""
        )
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Resultado registrado com sucesso"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Erro de validação ou agendamento já completado"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado — requer ROLE_ADMIN ou ROLE_EMPLOYEE"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
        })
        @PatchMapping("/{id}/result")
        @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
        public ResponseEntity<ApiResponse<AppointmentResponseDTO>> registerResult(
                        @Parameter(description = "ID do agendamento", example = "1")
                        @PathVariable Long id,
                        @Valid @RequestBody com.joao.adotec.dto.AppointmentResultDTO resultDto) {

                Appointment appointment = appointmentService.registerResult(id, resultDto);
                AppointmentResponseDTO responseData = appointmentMapper.toDTO(appointment);

                return ResponseEntity
                                .ok(ApiResponse.success("Appointment result registered successfully", responseData));
        }

    @Operation(
            summary = "Cancelar agendamento",
            description = """
                    Permite que um Adotante cancele seu próprio agendamento.
                    Não é possível cancelar agendamentos com status COMPLETED."""
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Agendamento cancelado com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Agendamento já completado — não pode ser cancelado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado ou não é o dono do agendamento"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    })
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADOPTER')")
    public ResponseEntity<ApiResponse<AppointmentResponseDTO>> cancelAppointment(
            @Parameter(description = "ID do agendamento a ser cancelado", example = "1")
            @PathVariable Long id,
            Authentication authentication) {

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long adopterId = userDetails.getId();

        Appointment appointment = appointmentService.cancelAppointment(id, adopterId);
        AppointmentResponseDTO responseData = appointmentMapper.toDTO(appointment);

        return ResponseEntity.ok(ApiResponse.success("Appointment canceled successfully", responseData));
    }
}
