package com.joao.adotec.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de entrada para solicitação de agendamento de visita")
public class AppointmentRequestDTO {

    @NotNull(message = "Pet ID is required")
    @Schema(description = "ID do pet que o adotante deseja visitar", example = "1")
    private Long petId;

    @NotBlank(message = "Time Slot ID is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}_\\d{2}:\\d{2}", message = "Time Slot ID deve estar no formato YYYY-MM-DD_HH:MM")
    @Schema(description = "Identificador do slot de horário desejado no formato YYYY-MM-DD_HH:MM", example = "2026-06-10_09:00")
    private String timeSlotId;
}
