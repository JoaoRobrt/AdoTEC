package com.joao.adotec.repositories;

import com.joao.adotec.models.Appointment;
import com.joao.adotec.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date AND a.startTime = :startTime AND a.status <> 'CANCELED'")
    int countActiveByDateAndStartTime(@Param("date") java.time.LocalDate date, @Param("startTime") java.time.LocalTime startTime);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Appointment a WHERE a.adopter = :adopter AND a.appointmentDate = :date AND a.startTime = :startTime AND a.status <> 'CANCELED'")
    boolean existsByAdopterAndAppointmentDateAndStartTime(@Param("adopter") User adopter, @Param("date") java.time.LocalDate date, @Param("startTime") java.time.LocalTime startTime);

    @EntityGraph(attributePaths = {"adopter", "pet", "employee"})
    Page<Appointment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"adopter", "pet", "employee"})
    Page<Appointment> findByAdopterOrderByCreatedAtDesc(User adopter, Pageable pageable);

    @EntityGraph(attributePaths = {"adopter", "pet", "employee"})
    Page<Appointment> findByEmployeeOrderByCreatedAtDesc(User employee, Pageable pageable);

    @EntityGraph(attributePaths = {"adopter", "pet", "employee"})
    @Query("SELECT a FROM Appointment a WHERE " +
           "(:status IS NULL OR a.status = :status) AND " +
           "((:unassigned = true AND a.employee IS NULL) OR " +
           "((:unassigned IS NULL OR :unassigned = false) AND (:employeeId IS NULL OR a.employee.userId = :employeeId))) AND " +
           "(:showCanceled = true OR a.status <> 'CANCELED' OR :status = 'CANCELED')")
    Page<Appointment> findAllWithFilters(
            @Param("status") com.joao.adotec.enums.AppointmentStatus status,
            @Param("employeeId") Long employeeId,
            @Param("unassigned") Boolean unassigned,
            @Param("showCanceled") Boolean showCanceled,
            Pageable pageable);

    @EntityGraph(attributePaths = {"adopter", "pet", "employee"})
    @Query("SELECT a FROM Appointment a WHERE a.employee IS NULL AND a.status NOT IN ('CANCELED', 'COMPLETED')")
    Page<Appointment> findUnassignedAppointments(Pageable pageable);

    @EntityGraph(attributePaths = {"adopter", "pet", "employee"})
    @Query("SELECT a FROM Appointment a WHERE a.employee.userId = :employeeId AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:showCanceled = true OR a.status <> 'CANCELED' OR :status = 'CANCELED')")
    Page<Appointment> findByEmployeeWithFilters(
            @Param("employeeId") Long employeeId,
            @Param("status") com.joao.adotec.enums.AppointmentStatus status,
            @Param("showCanceled") Boolean showCanceled,
            Pageable pageable);

    @EntityGraph(attributePaths = {"adopter", "pet", "employee"})
    @Query("SELECT a FROM Appointment a WHERE a.adopter.userId = :adopterId AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:showCanceled = true OR a.status <> 'CANCELED' OR :status = 'CANCELED')")
    Page<Appointment> findByAdopterWithFilters(
            @Param("adopterId") Long adopterId,
            @Param("status") com.joao.adotec.enums.AppointmentStatus status,
            @Param("showCanceled") Boolean showCanceled,
            Pageable pageable);
}
