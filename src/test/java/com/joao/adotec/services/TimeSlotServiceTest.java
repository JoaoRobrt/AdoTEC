package com.joao.adotec.services;

import com.joao.adotec.dto.TimeSlotResponseDTO;
import com.joao.adotec.models.TimeSlot;
import com.joao.adotec.repositories.TimeSlotRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimeSlotService — Unit Tests")
class TimeSlotServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private TimeSlotService timeSlotService;

    @Test
    @DisplayName("findAvailableByDate → Should return mapped TimeSlotResponseDTO list")
    void findAvailableByDate_shouldReturnMappedList() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 6, 15);
        TimeSlot slot1 = new TimeSlot(1L, date, LocalTime.of(9, 0), LocalTime.of(10, 0), 2, 0L, null);
        TimeSlot slot2 = new TimeSlot(2L, date, LocalTime.of(14, 0), LocalTime.of(15, 0), 3, 0L, null);

        given(timeSlotRepository.findAvailableByDate(date)).willReturn(Arrays.asList(slot1, slot2));

        // Act
        List<TimeSlotResponseDTO> result = timeSlotService.findAvailableByDate(date);

        // Assert
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).date()).isEqualTo(date);
        assertThat(result.get(0).startTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(result.get(0).endTime()).isEqualTo(LocalTime.of(10, 0));

        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).date()).isEqualTo(date);
        assertThat(result.get(1).startTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(result.get(1).endTime()).isEqualTo(LocalTime.of(15, 0));

        verify(timeSlotRepository).findAvailableByDate(date);
    }

    @Test
    @DisplayName("findAvailableByDate → Should return empty list when no slots available")
    void findAvailableByDate_whenNoSlots_shouldReturnEmptyList() {
        // Arrange
        LocalDate date = LocalDate.of(2026, 6, 15);
        given(timeSlotRepository.findAvailableByDate(date)).willReturn(Collections.emptyList());

        // Act
        List<TimeSlotResponseDTO> result = timeSlotService.findAvailableByDate(date);

        // Assert
        assertThat(result).isEmpty();
        verify(timeSlotRepository).findAvailableByDate(date);
    }

    @Test
    @DisplayName("findAvailableByDateRange → Should return mapped TimeSlotResponseDTO list")
    void findAvailableByDateRange_shouldReturnMappedList() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 6, 10);
        LocalDate endDate = LocalDate.of(2026, 6, 15);
        TimeSlot slot1 = new TimeSlot(1L, LocalDate.of(2026, 6, 11), LocalTime.of(9, 0), LocalTime.of(10, 0), 2, 0L, null);
        TimeSlot slot2 = new TimeSlot(2L, LocalDate.of(2026, 6, 14), LocalTime.of(14, 0), LocalTime.of(15, 0), 3, 0L, null);

        given(timeSlotRepository.findAvailableByDateBetween(startDate, endDate)).willReturn(Arrays.asList(slot1, slot2));

        // Act
        List<TimeSlotResponseDTO> result = timeSlotService.findAvailableByDateRange(startDate, endDate);

        // Assert
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).date()).isEqualTo(LocalDate.of(2026, 6, 11));
        
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).date()).isEqualTo(LocalDate.of(2026, 6, 14));

        verify(timeSlotRepository).findAvailableByDateBetween(startDate, endDate);
    }

    @Test
    @DisplayName("findAvailableByDateRange → Should return empty list when no slots available in range")
    void findAvailableByDateRange_whenNoSlots_shouldReturnEmptyList() {
        // Arrange
        LocalDate startDate = LocalDate.of(2026, 6, 10);
        LocalDate endDate = LocalDate.of(2026, 6, 15);
        given(timeSlotRepository.findAvailableByDateBetween(startDate, endDate)).willReturn(Collections.emptyList());

        // Act
        List<TimeSlotResponseDTO> result = timeSlotService.findAvailableByDateRange(startDate, endDate);

        // Assert
        assertThat(result).isEmpty();
        verify(timeSlotRepository).findAvailableByDateBetween(startDate, endDate);
    }
}
