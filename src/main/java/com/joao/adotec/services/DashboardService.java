package com.joao.adotec.services;

import com.joao.adotec.dto.DashboardMetricsDTO;
import com.joao.adotec.dto.commons.PageResponseDTO;
import com.joao.adotec.dto.commons.PageMetaDTO;
import com.joao.adotec.dto.AppointmentResponseDTO;
import com.joao.adotec.enums.AppRole;
import com.joao.adotec.enums.AppointmentStatus;
import com.joao.adotec.mappers.AppointmentMapper;
import com.joao.adotec.models.Appointment;
import com.joao.adotec.repositories.AppointmentRepository;
import com.joao.adotec.repositories.PetRepository;
import com.joao.adotec.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final AppointmentMapper appointmentMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "dashboardMetrics", key = "'admin-dashboard'")
    public DashboardMetricsDTO getDashboardMetrics() {
        log.info("Cache MISS — calculando dashboardMetrics");

        long petsAvailable = petRepository.countByIsAvailableForAdoptionTrueAndIsActiveTrue();
        long appointmentsTotal = appointmentRepository.countByStatusNot(AppointmentStatus.CANCELED);
        long pendingTotal = appointmentRepository.countByStatus(AppointmentStatus.PENDING);
        long pendingToday = appointmentRepository.countPendingToday(LocalDate.now());
        long unassignedTotal = appointmentRepository.countUnassigned();
        long employeesTotal = userRepository.countByRoles_RoleName(AppRole.ROLE_EMPLOYEE);

        return new DashboardMetricsDTO(
                petsAvailable,
                appointmentsTotal,
                pendingTotal,
                pendingToday,
                unassignedTotal,
                employeesTotal
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "dashboardUnassigned", key = "'unassigned'")
    public PageResponseDTO<AppointmentResponseDTO> getUnassignedAppointments() {
        log.info("Cache MISS — buscando unassigned");

        // Fetch top 5 unassigned appointments ordered by date/time
        Pageable topFive = PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "appointmentDate").and(Sort.by(Sort.Direction.ASC, "startTime")));
        List<AppointmentResponseDTO> appointments = appointmentRepository.findUnassignedAppointments(topFive)
                .stream()
                .map(appointmentMapper::toDTO)
                .toList();

        int totalElements = appointments.size();
        PageMetaDTO meta = new PageMetaDTO(
                0,
                5,
                (long) totalElements,
                1,
                true,
                true,
                java.util.Collections.emptyList()
        );

        return new PageResponseDTO<>(appointments, meta);
    }
}
