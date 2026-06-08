package com.joao.adotec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Métricas consolidadas do painel administrativo")
public class DashboardMetricsDTO {
    
    @Schema(description = "Total de pets ativos e disponíveis para adoção no sistema", example = "12")
    private long petsAvailable;
    
    @Schema(description = "Quantidade total de agendamentos registrados (exclui cancelados)", example = "25")
    private long appointmentsTotal;
    
    @Schema(description = "Quantidade total de agendamentos com status PENDING", example = "8")
    private long pendingTotal;
    
    @Schema(description = "Quantidade de agendamentos PENDING agendados para a data de hoje", example = "3")
    private long pendingToday;
    
    @Schema(description = "Quantidade total de agendamentos que ainda não têm funcionário atribuído", example = "5")
    private long unassignedTotal;
    
    @Schema(description = "Quantidade total de funcionários (ROLE_EMPLOYEE) cadastrados", example = "6")
    private long employeesTotal;
}
