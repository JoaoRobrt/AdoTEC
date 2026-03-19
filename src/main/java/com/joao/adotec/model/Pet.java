package com.joao.adotec.model;

import com.joao.adotec.enums.PetSize;

import java.time.Instant;

public class Pet {
    private Long petId;
    private String petName;
    private String description;
    private Integer ageInMonths;
    private PetSize size;
    private String photoUrl;
    private Instant createdAt;
}
