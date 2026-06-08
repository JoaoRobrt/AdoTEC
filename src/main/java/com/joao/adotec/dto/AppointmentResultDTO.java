package com.joao.adotec.dto;

import com.joao.adotec.enums.AdoptionResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para registro do resultado de uma visita de adoção")
public class AppointmentResultDTO {

    @NotNull(message = "Result is required")
    @Schema(description = "Resultado final do parecer da visita", example = "APPROVED")
    private AdoptionResult result;

    @Schema(description = "Observações detalhadas sobre a aprovação ou rejeição da adoção", example = "Candidato atende todos os requisitos de espaço e segurança.")
    private String notes;
}
