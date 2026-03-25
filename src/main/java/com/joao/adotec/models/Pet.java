package com.joao.adotec.models;

import com.joao.adotec.enums.PetSize;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
