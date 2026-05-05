package com.joao.adotec.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record TimeSlotSummaryDTO(
        Long timeSlotId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {
}
