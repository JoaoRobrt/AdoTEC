package com.joao.adotec.repositories;

import com.joao.adotec.models.TimeSlot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TimeSlot t WHERE t.timeSlotId = :id")
    Optional<TimeSlot> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT t FROM TimeSlot t WHERE t.date = :date AND (SELECT COUNT(a) FROM Appointment a WHERE a.timeSlot = t AND a.status <> 'CANCELED') < t.maxAppointments")
    List<TimeSlot> findAvailableByDate(@Param("date") LocalDate date);

    @Query("SELECT t FROM TimeSlot t WHERE t.date BETWEEN :startDate AND :endDate AND (SELECT COUNT(a) FROM Appointment a WHERE a.timeSlot = t AND a.status <> 'CANCELED') < t.maxAppointments")
    List<TimeSlot> findAvailableByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
