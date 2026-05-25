package com.joao.adotec.controllers;

import com.joao.adotec.dto.TimeSlotResponseDTO;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.services.TimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/timeslots")
@RequiredArgsConstructor
@Tag(name = "Time Slots", description = "Endpoints for querying available visit time slots")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @Operation(summary = "Get available time slots",
               description = "Retrieves available time slots for a specific date or a date range that still have booking capacity.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Time slots retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing or invalid date parameters")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotResponseDTO>>> getAvailableTimeSlots(
            @RequestParam(name = "date", required = false) LocalDate date,
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @RequestParam(name = "endDate", required = false) LocalDate endDate) {

        List<TimeSlotResponseDTO> availableSlots;

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("startDate não pode ser posterior a endDate");
            }
            availableSlots = timeSlotService.findAvailableByDateRange(startDate, endDate);
        }

        else if (date != null) {
            availableSlots = timeSlotService.findAvailableByDate(date);
        }
        else {
            throw new IllegalArgumentException("Você deve informar 'date' ou a combinação 'startDate' e 'endDate'");
        }

        return ResponseEntity.ok(ApiResponse.success("Time slots retrieved successfully", availableSlots));
    }
}
