package com.joao.adotec.models;

import com.joao.adotec.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tb_appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private com.joao.adotec.enums.AdoptionResult adoptionResult;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // -----------------------------------------------------------------------
    // Relationships
    // -----------------------------------------------------------------------

    /**
     * The adopter who scheduled the visit. Always required.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adopter_id", nullable = false)
    private User adopter;

    /**
     * The employee assigned to accompany the visit. Nullable (assigned later by admin).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private User employee;

    /**
     * The pet the adopter wants to visit.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    /**
     * The time slot chosen for the visit.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;
}
