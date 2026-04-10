package com.joao.adotec.dto;

import com.joao.adotec.enums.AdoptionResult;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResultDTO {

    @NotNull(message = "Result is required")
    private AdoptionResult result;

    private String notes;
}
