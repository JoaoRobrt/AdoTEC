package com.joao.adotec.services;

import com.joao.adotec.enums.AppointmentStatus;
import com.joao.adotec.exceptions.domain.BusinessException;
import com.joao.adotec.exceptions.domain.ResourceNotFoundException;
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

    @Mock
    private com.joao.adotec.repositories.PetRepository petRepository;

    @Mock
    private com.joao.adotec.repositories.UserRepository userRepository;

    @Mock
    private com.joao.adotec.repositories.TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    @DisplayName("createAppointment → RN01: Should throw BusinessException when time slot is full")
    void createAppointment_whenTimeSlotIsFull_shouldThrowBusinessException() {
        // Arrange
        User adopter = new User();
        Pet pet = new Pet();
        TimeSlot timeSlot = new TimeSlot(1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 2, 0L, null);

        given(appointmentRepository.existsByAdopterAndTimeSlot(adopter, timeSlot)).willReturn(false);
        given(appointmentRepository.countActiveByTimeSlot(timeSlot)).willReturn(2); // Full capacity

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
        TimeSlot timeSlot = new TimeSlot(1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 2, 0L, null);

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
        TimeSlot timeSlot = new TimeSlot(1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), 2, 0L, null);

        given(appointmentRepository.existsByAdopterAndTimeSlot(adopter, timeSlot)).willReturn(false);
        given(appointmentRepository.countActiveByTimeSlot(timeSlot)).willReturn(1); // Not full

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

    @Test
    @DisplayName("registerResult → Should set AdoptionResult, notes, change status to COMPLETED, and change Pet availability if APPROVED")
    void registerResult_whenApproved_shouldUpdateAppointmentAndPet() {
        // Arrange
        Long appointmentId = 1L;
        com.joao.adotec.dto.AppointmentResultDTO resultDto = new com.joao.adotec.dto.AppointmentResultDTO(
                com.joao.adotec.enums.AdoptionResult.APPROVED, "Great adoption!");
        
        Pet pet = new Pet();
        pet.setIsAvailableForAdoption(true);
        
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);
        appointment.setPet(pet);
        appointment.setStatus(AppointmentStatus.PENDING);

        given(appointmentRepository.findById(appointmentId)).willReturn(java.util.Optional.of(appointment));
        given(appointmentRepository.save(any(Appointment.class))).willReturn(appointment);

        // Act
        Appointment result = appointmentService.registerResult(appointmentId, resultDto);

        // Assert
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        assertThat(result.getAdoptionResult()).isEqualTo(com.joao.adotec.enums.AdoptionResult.APPROVED);
        assertThat(result.getNotes()).isEqualTo("Great adoption!");
        assertThat(pet.getIsAvailableForAdoption()).isFalse();
    }

    @Test
    @DisplayName("registerResult → Should throw BusinessException if appointment is already COMPLETED")
    void registerResult_whenStatusIsCompleted_shouldThrowBusinessException() {
        // Arrange
        Long appointmentId = 1L;
        com.joao.adotec.dto.AppointmentResultDTO resultDto = new com.joao.adotec.dto.AppointmentResultDTO(
                com.joao.adotec.enums.AdoptionResult.APPROVED, "Attempt to update completed");

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);
        appointment.setStatus(AppointmentStatus.COMPLETED); // Already completed

        given(appointmentRepository.findById(appointmentId)).willReturn(java.util.Optional.of(appointment));

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.registerResult(appointmentId, resultDto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cannot update result for an appointment that is already COMPLETED.");
    }
    @Test
    @DisplayName("assignEmployee → Should throw BusinessException when user is not an employee")
    void assignEmployee_whenUserIsNotEmployee_shouldThrowBusinessException() {
        // Arrange
        Long appointmentId = 1L;
        Long employeeId = 2L;

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);

        User nonEmployee = new User();
        nonEmployee.setUserId(employeeId);
        nonEmployee.getRoles().add(new com.joao.adotec.models.Role(com.joao.adotec.enums.AppRole.ROLE_ADOPTER));

        given(appointmentRepository.findById(appointmentId)).willReturn(java.util.Optional.of(appointment));
        given(userRepository.findById(employeeId)).willReturn(java.util.Optional.of(nonEmployee));

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.assignEmployee(appointmentId, employeeId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("User provided is not an employee.");
    }

    @Test
    @DisplayName("assignEmployee → Should successfully assign employee when user has ROLE_EMPLOYEE")
    void assignEmployee_whenUserIsEmployee_shouldAssignEmployee() {
        // Arrange
        Long appointmentId = 1L;
        Long employeeId = 2L;

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);

        User employeeUser = new User();
        employeeUser.setUserId(employeeId);
        employeeUser.getRoles().add(new com.joao.adotec.models.Role(com.joao.adotec.enums.AppRole.ROLE_EMPLOYEE));

        given(appointmentRepository.findById(appointmentId)).willReturn(java.util.Optional.of(appointment));
        given(userRepository.findById(employeeId)).willReturn(java.util.Optional.of(employeeUser));
        given(appointmentRepository.save(any(Appointment.class))).willReturn(appointment);

        // Act
        Appointment result = appointmentService.assignEmployee(appointmentId, employeeId);

        // Assert
        assertThat(result.getEmployee()).isEqualTo(employeeUser);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("cancelAppointment → Should successfully cancel when adopter owns the appointment")
    void cancelAppointment_whenOwner_shouldCancel() {
        Long appointmentId = 1L;
        Long loggedUserId = 10L;

        User adopter = new User();
        adopter.setUserId(loggedUserId);

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);
        appointment.setAdopter(adopter);
        appointment.setStatus(AppointmentStatus.PENDING);

        given(appointmentRepository.findById(appointmentId)).willReturn(java.util.Optional.of(appointment));
        given(appointmentRepository.save(any(Appointment.class))).willReturn(appointment);

        Appointment result = appointmentService.cancelAppointment(appointmentId, loggedUserId);

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CANCELED);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    @DisplayName("cancelAppointment → Should throw AccessDeniedException when other user tries to cancel")
    void cancelAppointment_whenNotOwner_shouldThrowAccessDeniedException() {
        Long appointmentId = 1L;
        Long loggedUserId = 99L; // Different from owner

        User adopter = new User();
        adopter.setUserId(10L); // Owner is 10

        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);
        appointment.setAdopter(adopter);

        given(appointmentRepository.findById(appointmentId)).willReturn(java.util.Optional.of(appointment));

        assertThatThrownBy(() -> appointmentService.cancelAppointment(appointmentId, loggedUserId))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessage("You do not have permission to cancel this appointment.");
    }

    @Test
    @DisplayName("getAppointmentById → Should return appointment when it exists")
    void getAppointmentById_whenExists_shouldReturnAppointment() {
        Long appointmentId = 1L;
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(appointmentId);
        
        User adopter = new User();
        adopter.setUserId(10L);
        appointment.setAdopter(adopter);

        com.joao.adotec.security.services.UserDetailsImpl userDetails = new com.joao.adotec.security.services.UserDetailsImpl(
                10L, "User", "user@test.com", "pass", true,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADOPTER")));

        given(appointmentRepository.findById(appointmentId)).willReturn(java.util.Optional.of(appointment));

        Appointment result = appointmentService.getAppointmentById(appointmentId, userDetails);

        assertThat(result).isNotNull();
        assertThat(result.getAppointmentId()).isEqualTo(appointmentId);
    }

    @Test
    @DisplayName("getAppointmentById → Should throw ResourceNotFoundException when not found")
    void getAppointmentById_whenNotFound_shouldThrowResourceNotFoundException() {
        Long appointmentId = 999L;
        com.joao.adotec.security.services.UserDetailsImpl userDetails = new com.joao.adotec.security.services.UserDetailsImpl(
                10L, "User", "user@test.com", "pass", true,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADOPTER")));

        given(appointmentRepository.findById(appointmentId)).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> appointmentService.getAppointmentById(appointmentId, userDetails))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Appointment")
                .hasMessageContaining("999");
    }
}
