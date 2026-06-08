package com.joao.adotec.controllers;

import com.joao.adotec.dto.TimeSlotResponseDTO;
import com.joao.adotec.dto.response.ApiResponse;
import com.joao.adotec.services.TimeSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "Time Slots", description = "Consulta de horários disponíveis para agendamento de visitas")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @Operation(
            summary = "Horários disponíveis",
            description = """
                    Retorna os horários de visita que ainda possuem vagas para uma data específica ou um intervalo de datas. **Acesso público.**

                    Utilize **uma** das opções:
                    - `date` — busca horários de uma data específica
                    - `startDate` + `endDate` — busca horários de um intervalo de datas

                    ### Exemplos
                    - `GET /timeslots?date=2026-06-10`
                    - `GET /timeslots?startDate=2026-06-10&endDate=2026-06-14`"""
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Horários retornados com sucesso"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Parâmetros de data ausentes ou inválidos")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<TimeSlotResponseDTO>>> getAvailableTimeSlots(
            @Parameter(description = "Data específica para buscar horários (formato ISO: YYYY-MM-DD)", example = "2026-06-10")
            @RequestParam(name = "date", required = false) LocalDate date,
            @Parameter(description = "Data inicial do intervalo (formato ISO: YYYY-MM-DD)", example = "2026-06-10")
            @RequestParam(name = "startDate", required = false) LocalDate startDate,
            @Parameter(description = "Data final do intervalo (formato ISO: YYYY-MM-DD)", example = "2026-06-14")
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
