package com.joao.adotec.controllers;

import com.joao.adotec.dto.AppointmentResponseDTO;
import com.joao.adotec.dto.DashboardMetricsDTO;
import com.joao.adotec.dto.commons.PageResponseDTO;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.services.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Métricas consolidadas e listas operacionais para o painel administrativo")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
            summary = "Métricas do dashboard",
            description = """
                    Retorna estatísticas consolidadas para o painel administrativo. **Requer ROLE_ADMIN.**

                    Dados retornados em cache Redis (TTL configurável em `cache.dashboard.ttl-seconds`).

                    ### Métricas retornadas
                    | Campo              | Descrição                                                  |
                    |--------------------|-------------------------------------------------------------|
                    | `petsAvailable`    | Quantidade de pets ativos e disponíveis para adoção          |
                    | `appointmentsTotal`| Total de agendamentos (exceto cancelados)                    |
                    | `pendingTotal`     | Total de agendamentos com status PENDING                     |
                    | `pendingToday`     | Agendamentos PENDING agendados para hoje                     |
                    | `unassignedTotal`  | Agendamentos sem funcionário atribuído                       |
                    | `employeesTotal`   | Quantidade de usuários com ROLE_EMPLOYEE                     |"""
    )
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<DashboardMetricsDTO>> getDashboardMetrics() {
        DashboardMetricsDTO metrics = dashboardService.getDashboardMetrics();
        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics fetched successfully", metrics));
    }

    @Operation(
            summary = "Agendamentos não atribuídos",
            description = """
                    Retorna os **5 agendamentos mais antigos** que ainda não possuem funcionário atribuído,
                    ordenados por `appointmentDate ASC` e `startTime ASC`. **Requer ROLE_ADMIN.**

                    Ideal para o card operacional do painel administrativo.
                    Dados retornados em cache Redis (TTL configurável em `cache.dashboard.ttl-seconds`).

                    A resposta segue o formato padrão `PageResponseDTO` com `content` e `pagination`."""
    )
    @GetMapping("/unassigned-appointments")
    public ResponseEntity<ApiResponse<PageResponseDTO<AppointmentResponseDTO>>> getUnassignedAppointments() {
        PageResponseDTO<AppointmentResponseDTO> pageResponse = dashboardService.getUnassignedAppointments();
        return ResponseEntity.ok(ApiResponse.success("Unassigned appointments fetched successfully", pageResponse));
    }
}
