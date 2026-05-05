package com.joao.adotec.services;

import com.joao.adotec.dto.TimeSlotResponseDTO;
import com.joao.adotec.models.TimeSlot;
import com.joao.adotec.repositories.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    /**
     * Returns time slots for a given date that still have capacity
     * (current appointments < maxAppointments).
     */
    @Transactional(readOnly = true)
    public List<TimeSlotResponseDTO> findAvailableByDate(LocalDate date) {
        List<TimeSlot> slots = timeSlotRepository.findByDate(date);

        return slots.stream()
                .filter(slot -> slot.getAppointments().size() < slot.getMaxAppointments())
                .map(slot -> new TimeSlotResponseDTO(
                        slot.getTimeSlotId(),
                        slot.getDate(),
                        slot.getStartTime(),
                        slot.getEndTime()
                ))
                .toList();
    }

    /**
     * Returns time slots between two dates that still have capacity
     * (current appointments < maxAppointments).
     */
    @Transactional(readOnly = true)
    public List<TimeSlotResponseDTO> findAvailableByDateRange(LocalDate startDate, LocalDate endDate) {
        List<TimeSlot> slots = timeSlotRepository.findByDateBetween(startDate, endDate);

        return slots.stream()
                .filter(slot -> slot.getAppointments().size() < slot.getMaxAppointments())
                .map(slot -> new TimeSlotResponseDTO(
                        slot.getTimeSlotId(),
                        slot.getDate(),
                        slot.getStartTime(),
                        slot.getEndTime()
                ))
                .toList();
    }
}
