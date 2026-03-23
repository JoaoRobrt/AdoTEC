package com.joao.adotec.models;

import com.joao.adotec.enums.PetSize;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pet {
    private Long petId;
    private String petName;
    private String description;
    private Integer ageInMonths;
    private PetSize size;
    private String photoUrl;
    private Instant createdAt;

}
