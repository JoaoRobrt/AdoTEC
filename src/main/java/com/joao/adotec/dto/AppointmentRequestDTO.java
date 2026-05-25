package com.joao.adotec.dto;

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
public class AppointmentRequestDTO {

    @NotNull(message = "Pet ID is required")
    private Long petId;

    @NotBlank(message = "Time Slot ID is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}_\\d{2}:\\d{2}", message = "Time Slot ID deve estar no formato YYYY-MM-DD_HH:MM")
    private String timeSlotId;
}
