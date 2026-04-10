package com.joao.adotec.repositories;

import com.joao.adotec.models.Appointment;
import com.joao.adotec.models.TimeSlot;
import com.joao.adotec.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    int countByTimeSlot(TimeSlot timeSlot);

    boolean existsByAdopterAndTimeSlot(User adopter, TimeSlot timeSlot);

    java.util.List<Appointment> findByAdopterOrderByCreatedAtDesc(User adopter);

    java.util.List<Appointment> findByEmployeeOrderByCreatedAtDesc(User employee);
}
