package com.joao.adotec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Representa um horário de visita disponível para agendamento")
public record TimeSlotResponseDTO(
        @Schema(description = "Identificador do horário no formato YYYY-MM-DD_HH:MM", example = "2026-06-10_09:00")
        String slotId,
        
        @Schema(description = "Data do atendimento", example = "2026-06-10")
        LocalDate date,
        
        @Schema(description = "Hora de início do atendimento", example = "09:00:00")
        LocalTime startTime,
        
        @Schema(description = "Hora de término do atendimento", example = "10:00:00")
        LocalTime endTime,
        
        @Schema(description = "Número de vagas de agendamento ainda disponíveis para este horário", example = "2")
        int vagasRestantes
) {
}
