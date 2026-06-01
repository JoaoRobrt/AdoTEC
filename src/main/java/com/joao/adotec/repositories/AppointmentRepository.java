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

    @EntityGraph(attributePaths = {"adopter", "pet"})
    Page<Appointment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"adopter", "pet"})
    Page<Appointment> findByAdopterOrderByCreatedAtDesc(User adopter, Pageable pageable);

    @EntityGraph(attributePaths = {"adopter", "pet"})
    Page<Appointment> findByEmployeeOrderByCreatedAtDesc(User employee, Pageable pageable);
}
