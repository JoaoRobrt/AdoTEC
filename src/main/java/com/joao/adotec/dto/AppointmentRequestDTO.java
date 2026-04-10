package com.joao.adotec.dto;

import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Time Slot ID is required")
    private Long timeSlotId;
}
