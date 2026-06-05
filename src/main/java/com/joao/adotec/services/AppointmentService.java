package com.joao.adotec.services;

import com.joao.adotec.enums.AppointmentStatus;
import com.joao.adotec.exceptions.domain.BusinessException;
import com.joao.adotec.exceptions.domain.ResourceNotFoundException;
import com.joao.adotec.models.Appointment;
import com.joao.adotec.models.Pet;
import com.joao.adotec.config.VisitScheduleProperties;
import com.joao.adotec.exceptions.domain.DuplicateAppointmentException;
import com.joao.adotec.exceptions.domain.SlotUnavailableException;
import com.joao.adotec.models.Appointment;
import com.joao.adotec.models.Pet;
import com.joao.adotec.models.User;
import com.joao.adotec.repositories.AppointmentRepository;
import com.joao.adotec.repositories.PetRepository;
import com.joao.adotec.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final VisitScheduleProperties scheduleProperties;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboardMetrics", allEntries = true),
            @CacheEvict(value = "dashboardUnassigned", allEntries = true)
    })
    public Appointment createAppointment(Long adopterId, Long petId, String timeSlotId) {
        User adopter = userRepository.findById(adopterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adopterId));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));

        String[] parts = timeSlotId.split("_");
        if (parts.length != 2) {
            throw new BusinessException("Formato de timeSlotId inválido. Use YYYY-MM-DD_HH:MM");
        }
        LocalDate date = LocalDate.parse(parts[0]);
        LocalTime startTime = LocalTime.parse(parts[1]);

        return createAppointment(adopter, pet, date, startTime);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboardMetrics", allEntries = true),
            @CacheEvict(value = "dashboardUnassigned", allEntries = true)
    })
    public Appointment createAppointment(User adopter, Pet pet, LocalDate appointmentDate, LocalTime startTime) {

        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new BusinessException(
                    "Cannot schedule an appointment for a past date: " + appointmentDate);
        }

        VisitScheduleProperties.Slot configuredSlot = scheduleProperties.getSlots().stream()
                .filter(s -> s.getStart().equals(startTime))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Horário inválido ou não configurado."));

        if (appointmentRepository.existsByAdopterAndAppointmentDateAndStartTime(adopter, appointmentDate, startTime)) {
            throw new DuplicateAppointmentException();
        }

        int currentAppointments = appointmentRepository.countActiveByDateAndStartTime(appointmentDate, startTime);
        if (currentAppointments >= configuredSlot.getCapacity()) {
            throw new SlotUnavailableException();
        }

        Appointment appointment = new Appointment();
        appointment.setAdopter(adopter);
        appointment.setPet(pet);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setStartTime(startTime);
        appointment.setEndTime(configuredSlot.getEnd());
        appointment.setStatus(AppointmentStatus.PENDING);

        return appointmentRepository.save(appointment);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboardMetrics", allEntries = true),
            @CacheEvict(value = "dashboardUnassigned", allEntries = true)
    })
    public Appointment assignEmployee(Long appointmentId, Long employeeId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        User employee = getEmployeeValidated(employeeId);

        appointment.setEmployee(employee);

        return appointmentRepository.save(appointment);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> getAppointmentsByAdopter(Long adopterId, AppointmentStatus status, Boolean showCanceled, Pageable pageable) {
        User adopter = userRepository.findById(adopterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", adopterId));
        Pageable mappedPageable = mapPageableSort(pageable);
        return appointmentRepository.findByAdopterWithFilters(adopterId, status, showCanceled, mappedPageable);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> getAppointmentsByEmployee(Long employeeId, AppointmentStatus status, Boolean showCanceled, Pageable pageable) {
        getEmployeeValidated(employeeId);
        Pageable mappedPageable = mapPageableSort(pageable);
        return appointmentRepository.findByEmployeeWithFilters(employeeId, status, showCanceled, mappedPageable);
    }

    private User getEmployeeValidated(Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", employeeId));

        boolean isEmployee = employee.getRoles().stream()
                .anyMatch(role -> role.getRoleName() == com.joao.adotec.enums.AppRole.ROLE_EMPLOYEE);
        if (!isEmployee) {
            throw new BusinessException("User provided is not an employee.");
        }
        return employee;
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long appointmentId, com.joao.adotec.security.services.UserDetailsImpl userDetails) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        boolean isAdminOrEmployee = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (!isAdminOrEmployee && !appointment.getAdopter().getUserId().equals(userDetails.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to view this appointment.");
        }

        return appointment;
    }

    @Transactional(readOnly = true)
    public Page<Appointment> getAllAppointments(AppointmentStatus status, Long employeeId, Boolean unassigned, Boolean showCanceled, Pageable pageable) {
        Pageable mappedPageable = mapPageableSort(pageable);
        return appointmentRepository.findAllWithFilters(status, employeeId, unassigned, showCanceled, mappedPageable);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> getUnassignedAppointments(Pageable pageable) {
        Pageable mappedPageable = mapPageableSort(pageable);
        return appointmentRepository.findUnassignedAppointments(mappedPageable);
    }

    private Pageable mapPageableSort(Pageable pageable) {
        if (pageable.getSort().isSorted()) {
            java.util.List<org.springframework.data.domain.Sort.Order> orders = pageable.getSort().stream()
                .map(order -> {
                    if ("petName".equals(order.getProperty())) {
                        return new org.springframework.data.domain.Sort.Order(order.getDirection(), "pet.petName");
                    }
                    return order;
                })
                .collect(java.util.stream.Collectors.toList());
            return org.springframework.data.domain.PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                org.springframework.data.domain.Sort.by(orders)
            );
        }
        return pageable;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboardMetrics", allEntries = true),
            @CacheEvict(value = "dashboardUnassigned", allEntries = true)
    })
    public Appointment registerResult(Long appointmentId, com.joao.adotec.dto.AppointmentResultDTO resultDto) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException("Cannot update result for an appointment that is already COMPLETED.");
        }

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

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "dashboardMetrics", allEntries = true),
            @CacheEvict(value = "dashboardUnassigned", allEntries = true)
    })
    public Appointment cancelAppointment(Long appointmentId, Long loggedUserId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (!appointment.getAdopter().getUserId().equals(loggedUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to cancel this appointment.");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel an appointment that is already COMPLETED.");
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
        return appointmentRepository.save(appointment);
    }
}
