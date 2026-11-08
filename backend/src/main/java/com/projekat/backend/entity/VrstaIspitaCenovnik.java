package com.projekat.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Asocijativna klasa između VrstaIspita i Cenovnik.
 * Ne sme se mapirati kao OneToOne - veza ide isključivo preko dva ManyToOne.
 */
@Entity
@Table(name = "vrsta_ispita_cenovnik")
@Getter
@Setter
@NoArgsConstructor
public class VrstaIspitaCenovnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean aktivna = true;

    @ManyToOne
    @JoinColumn(name = "vrsta_ispita_id", nullable = false)
    private VrstaIspita vrstaIspita;

    @ManyToOne
    @JoinColumn(name = "cenovnik_id", nullable = false)
    private Cenovnik cenovnik;
}
