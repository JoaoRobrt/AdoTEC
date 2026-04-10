package com.joao.adotec.services;

import com.joao.adotec.enums.AppointmentStatus;
import com.joao.adotec.exceptions.domain.BusinessException;
import com.joao.adotec.models.Appointment;
import com.joao.adotec.models.Pet;
import com.joao.adotec.models.TimeSlot;
import com.joao.adotec.models.User;
import com.joao.adotec.repositories.AppointmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService — Unit Tests")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    @DisplayName("createAppointment → RN01: Should throw BusinessException when time slot is full")
    void createAppointment_whenTimeSlotIsFull_shouldThrowBusinessException() {
        // Arrange
        User adopter = new User();
        Pet pet = new Pet();
        TimeSlot timeSlot = new TimeSlot(1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 2, null);

        given(appointmentRepository.existsByAdopterAndTimeSlot(adopter, timeSlot)).willReturn(false);
        given(appointmentRepository.countByTimeSlot(timeSlot)).willReturn(2); // Full capacity

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.createAppointment(adopter, pet, timeSlot))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Time slot has reached its maximum capacity.");
    }

    @Test
    @DisplayName("createAppointment → RN02: Should throw BusinessException when adopter already has an appointment")
    void createAppointment_whenAdopterAlreadyHasAppointment_shouldThrowBusinessException() {
        // Arrange
        User adopter = new User();
        Pet pet = new Pet();
        TimeSlot timeSlot = new TimeSlot(1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 2, null);

        given(appointmentRepository.existsByAdopterAndTimeSlot(adopter, timeSlot)).willReturn(true); // Already has appointment

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.createAppointment(adopter, pet, timeSlot))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Adopter already has an appointment for this time slot.");
    }

    @Test
    @DisplayName("createAppointment → Should successfully create appointment when valid")
    void createAppointment_whenValid_shouldCreateAppointment() {
        // Arrange
        User adopter = new User();
        Pet pet = new Pet();
        TimeSlot timeSlot = new TimeSlot(1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 2, null);

        given(appointmentRepository.existsByAdopterAndTimeSlot(adopter, timeSlot)).willReturn(false);
        given(appointmentRepository.countByTimeSlot(timeSlot)).willReturn(1); // Not full

        Appointment savedAppointment = new Appointment();
        savedAppointment.setAdopter(adopter);
        savedAppointment.setPet(pet);
        savedAppointment.setTimeSlot(timeSlot);
        savedAppointment.setStatus(AppointmentStatus.PENDING);
        
        given(appointmentRepository.save(any(Appointment.class))).willReturn(savedAppointment);

        // Act
        Appointment result = appointmentService.createAppointment(adopter, pet, timeSlot);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(result.getAdopter()).isEqualTo(adopter);
        assertThat(result.getPet()).isEqualTo(pet);
        assertThat(result.getTimeSlot()).isEqualTo(timeSlot);
        verify(appointmentRepository).save(any(Appointment.class));
    }
}
