package com.joao.adotec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Resumo dos dados de data e hora do agendamento")
public record TimeSlotSummaryDTO(
        @Schema(description = "ID do slot de horário formatado como YYYY-MM-DD_HH:MM", example = "2026-06-10_09:00")
        String timeSlotId,
        
        @Schema(description = "Data agendada", example = "2026-06-10")
        LocalDate date,
        
        @Schema(description = "Hora de início da visita", example = "09:00:00")
        LocalTime startTime,
        
        @Schema(description = "Hora de término da visita", example = "10:00:00")
        LocalTime endTime
) {
}
