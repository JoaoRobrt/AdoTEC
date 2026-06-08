package com.joao.adotec.dto;

import com.joao.adotec.enums.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representa um agendamento do sistema")
public class AppointmentResponseDTO {
    
    @Schema(description = "ID único do agendamento", example = "1")
    private Long appointmentId;
    
    @Schema(description = "Status atual do agendamento", example = "PENDING")
    private AppointmentStatus status;
    
    @Schema(description = "Data e hora de criação do agendamento", example = "2026-06-08T18:54:00Z")
    private Instant createdAt;
    
    @Schema(description = "Resultado final da visita de adoção", example = "APPROVED")
    private com.joao.adotec.enums.AdoptionResult adoptionResult;
    
    @Schema(description = "Observações ou parecer do funcionário sobre a visita", example = "Família demonstra grande afeto e possui espaço telado.")
    private String notes;
    
    @Schema(description = "ID do adotante", example = "5")
    private Long adopterId;
    
    @Schema(description = "Nome do adotante", example = "Adopter User")
    private String adopterName;

    @Schema(description = "ID do funcionário encarregado da visita", example = "2")
    private Long employeeId;
    
    @Schema(description = "Nome do funcionário encarregado", example = "Employee User")
    private String employeeName;

    @Schema(description = "ID do pet a ser visitado", example = "1")
    private Long petId;
    
    @Schema(description = "Nome do pet", example = "Rex")
    private String petName;

    @Schema(description = "Informações detalhadas do slot de data/hora da visita")
    private TimeSlotSummaryDTO timeSlot;
}
