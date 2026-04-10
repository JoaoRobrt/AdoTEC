package com.joao.adotec.services;

import com.joao.adotec.enums.AppointmentStatus;
import com.joao.adotec.exceptions.domain.BusinessException;
import com.joao.adotec.exceptions.domain.ResourceNotFoundException;
import com.joao.adotec.models.Appointment;
import com.joao.adotec.models.Pet;
import com.joao.adotec.models.TimeSlot;
import com.joao.adotec.models.User;
import com.joao.adotec.repositories.AppointmentRepository;
import com.joao.adotec.repositories.PetRepository;
import com.joao.adotec.repositories.TimeSlotRepository;
import com.joao.adotec.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final TimeSlotRepository timeSlotRepository;

    @Transactional
    public Appointment createAppointment(Long adopterId, Long petId, Long timeSlotId) {
        User adopter = userRepository.findById(adopterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adopterId));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));

        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSlot", timeSlotId));

        return createAppointment(adopter, pet, timeSlot);
    }

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

    @Transactional
    public Appointment assignEmployee(Long appointmentId, Long employeeId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", employeeId));

        appointment.setEmployee(employee);

        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> getAppointmentsByAdopter(Long adopterId, Pageable pageable) {
        User adopter = userRepository.findById(adopterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adopterId));
        return appointmentRepository.findByAdopterOrderByCreatedAtDesc(adopter, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> getAppointmentsByEmployee(Long employeeId, Pageable pageable) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", employeeId));
        return appointmentRepository.findByEmployeeOrderByCreatedAtDesc(employee, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    @Transactional
    public Appointment registerResult(Long appointmentId, com.joao.adotec.dto.AppointmentResultDTO resultDto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        appointment.setAdoptionResult(resultDto.getResult());
        appointment.setNotes(resultDto.getNotes());
        appointment.setStatus(AppointmentStatus.COMPLETED);

        if (resultDto.getResult() == com.joao.adotec.enums.AdoptionResult.APPROVED) {
            Pet pet = appointment.getPet();
            pet.setIsAvailableForAdoption(false);
            petRepository.save(pet);
        }

        return appointmentRepository.save(appointment);
    }
}
