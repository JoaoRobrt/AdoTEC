package com.joao.adotec.services;

import com.joao.adotec.enums.AppointmentStatus;
import com.joao.adotec.exceptions.domain.BusinessException;
import com.joao.adotec.models.Appointment;
import com.joao.adotec.models.Pet;
import com.joao.adotec.models.TimeSlot;
import com.joao.adotec.models.User;
import com.joao.adotec.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    @Transactional
    public Appointment createAppointment(User adopter, Pet pet, TimeSlot timeSlot) {
        if (appointmentRepository.existsByAdopterAndTimeSlot(adopter, timeSlot)) {
            throw new BusinessException("Adopter already has an appointment for this time slot.");
        }

        int currentAppointments = appointmentRepository.countByTimeSlot(timeSlot);
        if (currentAppointments >= timeSlot.getMaxAppointments()) {
            throw new BusinessException("Time slot has reached its maximum capacity.");
        }

        Appointment appointment = new Appointment();
        appointment.setAdopter(adopter);
        appointment.setPet(pet);
        appointment.setTimeSlot(timeSlot);
        appointment.setStatus(AppointmentStatus.PENDING);

        return appointmentRepository.save(appointment);
    }
}
