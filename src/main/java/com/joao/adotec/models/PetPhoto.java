package com.joao.adotec.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "tb_pet_photos")
@Getter
@Setter
@NoArgsConstructor
public class PetPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long photoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String publicId;

    @Column(nullable = false)
    private Boolean isPrimary = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    public PetPhoto(Pet pet, String url, String publicId, Boolean isPrimary) {
        this.pet = pet;
        this.url = url;
        this.publicId = publicId;
        this.isPrimary = isPrimary;
    }
}
