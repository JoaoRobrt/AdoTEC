package com.joao.adotec.controllers;

import com.joao.adotec.dto.TimeSlotResponseDTO;
import com.joao.adotec.exceptions.handler.GlobalExceptionHandler;
import com.joao.adotec.services.TimeSlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TDD — RED phase.
 * These tests target TimeSlotController which does NOT exist yet.
 * They must fail at compile time until the controller is implemented.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TimeSlotController — Available Time Slots Tests")
class TimeSlotControllerTest {

    @Mock
    private TimeSlotService timeSlotService;

    @InjectMocks
    private TimeSlotController timeSlotController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(timeSlotController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /timeslots?date=2026-06-15 → 200 with available time slots")
    void findAvailableByDate_validDate_returns200WithTimeSlots() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 15);

        List<TimeSlotResponseDTO> mockSlots = List.of(
                new TimeSlotResponseDTO(1L, date, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                new TimeSlotResponseDTO(2L, date, LocalTime.of(14, 0), LocalTime.of(15, 0))
        );

        given(timeSlotService.findAvailableByDate(date)).willReturn(mockSlots);

        mockMvc.perform(get("/timeslots").param("date", "2026-06-15"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Time slots retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].startTime").value("14:00:00"));
    }

    @Test
    @DisplayName("GET /timeslots?date=2026-06-15 → 200 with empty list when no slots available")
    void findAvailableByDate_noSlots_returns200WithEmptyList() throws Exception {
        LocalDate date = LocalDate.of(2026, 6, 15);

        given(timeSlotService.findAvailableByDate(date)).willReturn(List.of());

        mockMvc.perform(get("/timeslots").param("date", "2026-06-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /timeslots (no date param) → 400 Bad Request")
    void findAvailableByDate_missingDate_returns400() throws Exception {
        mockMvc.perform(get("/timeslots"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 200 e lista de TimeSlots disponíveis quando informar startDate e endDate")
    void getAvailableTimeSlots_WithDateRange_Returns200() throws Exception {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30); // Busca de um mês inteiro
        
        TimeSlotResponseDTO dummyResponse = new TimeSlotResponseDTO(
                2L, 
                LocalDate.of(2026, 6, 15), 
                LocalTime.of(14, 0), 
                LocalTime.of(15, 0)
        );
        
        // Agora o Service terá um método focado em range de datas
        given(timeSlotService.findAvailableByDateRange(startDate, endDate))
                .willReturn(List.of(dummyResponse));

        // Act & Assert
        mockMvc.perform(get("/timeslots")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-06-30")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(2L))
                .andExpect(jsonPath("$.data[0].date").value("2026-06-15"));
    }
}
