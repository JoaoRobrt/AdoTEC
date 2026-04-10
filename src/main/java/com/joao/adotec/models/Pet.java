package com.joao.adotec.models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.joao.adotec.enums.PetSize;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "tb_pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long petId;

    @Column(name = "pet_name", nullable = false)
    private String petName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "age_in_months")
    private Integer ageInMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PetSize size;

    private String photoUrl;

    @Column(nullable = false)
    private Boolean isAvailableForAdoption = true;

    /**
     * Soft-delete flag. When {@code false}, the pet is logically deleted.
     * Appointments linked to this pet are preserved for audit/history (RF15).
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Cascade kept for loading/navigation only; deletion is now soft, so
    // orphanRemoval is removed to prevent accidental hard-deletes of appointments.
    @OneToMany(mappedBy = "pet", fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();
}
