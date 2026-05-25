package com.joao.adotec.services;

import com.joao.adotec.config.VisitScheduleProperties;
import com.joao.adotec.dto.TimeSlotResponseDTO;
import com.joao.adotec.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final VisitScheduleProperties scheduleProperties;
    private final AppointmentRepository appointmentRepository;

    /**
     * Returns time slots for a given date that still have capacity
     * (current appointments < maxAppointments).
     */
    @Transactional(readOnly = true)
    public List<TimeSlotResponseDTO> findAvailableByDate(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            return List.of();
        }

        if (!scheduleProperties.getDays().contains(date.getDayOfWeek())) {
            return List.of();
        }

        List<TimeSlotResponseDTO> availableSlots = new ArrayList<>();

        for (VisitScheduleProperties.Slot slot : scheduleProperties.getSlots()) {
            int currentAppointments = appointmentRepository.countActiveByDateAndStartTime(date, slot.getStart());

            if (currentAppointments < slot.getCapacity()) {
                String slotId = date.toString() + "_" + slot.getStart().toString();
                int vagasRestantes = slot.getCapacity() - currentAppointments;
                availableSlots.add(new TimeSlotResponseDTO(
                        slotId,
                        date,
                        slot.getStart(),
                        slot.getEnd(),
                        vagasRestantes
                ));
            }
        }

        return availableSlots;
    }

    /**
     * Returns time slots between two dates that still have capacity
     * (current appointments < maxAppointments).
     */
    @Transactional(readOnly = true)
    public List<TimeSlotResponseDTO> findAvailableByDateRange(LocalDate startDate, LocalDate endDate) {
        List<TimeSlotResponseDTO> allSlots = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            allSlots.addAll(findAvailableByDate(currentDate));
            currentDate = currentDate.plusDays(1);
        }

        return allSlots;
    }
}
