package com.joao.adotec.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO representing a time slot available for scheduling an adoption visit.
 */
public record TimeSlotResponseDTO(
        Long id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {
}
