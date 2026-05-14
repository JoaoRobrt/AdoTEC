package com.joao.adotec.repositories;

import com.joao.adotec.models.Appointment;
import com.joao.adotec.models.TimeSlot;
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

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.timeSlot = :slot AND a.status <> 'CANCELED'")
    int countActiveByTimeSlot(@Param("slot") TimeSlot slot);

    boolean existsByAdopterAndTimeSlot(User adopter, TimeSlot timeSlot);

    @EntityGraph(attributePaths = {"adopter", "pet", "timeSlot"})
    Page<Appointment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"adopter", "pet", "timeSlot"})
    Page<Appointment> findByAdopterOrderByCreatedAtDesc(User adopter, Pageable pageable);

    @EntityGraph(attributePaths = {"adopter", "pet", "timeSlot"})
    Page<Appointment> findByEmployeeOrderByCreatedAtDesc(User employee, Pageable pageable);
}
