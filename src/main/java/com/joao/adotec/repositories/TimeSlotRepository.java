package com.joao.adotec.repositories;

import com.joao.adotec.models.TimeSlot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    /** Finds all time slots for the given date, eagerly loading appointments for capacity check. */
    @EntityGraph(attributePaths = {"appointments"})
    List<TimeSlot> findByDate(LocalDate date);

    /** Finds all time slots between two dates, eagerly loading appointments for capacity check. */
    @EntityGraph(attributePaths = {"appointments"})
    List<TimeSlot> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
