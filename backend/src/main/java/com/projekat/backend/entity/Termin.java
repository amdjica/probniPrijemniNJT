package com.projekat.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "termin")
@Getter
@Setter
@NoArgsConstructor
public class Termin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate datum;

    @Column(nullable = false)
    private String vremePocetka;

    @Column(length = 500)
    private String napomena;

    @Column(nullable = false)
    private Boolean dostupan = true;

    @ManyToOne
    @JoinColumn(name = "vrsta_ispita_id", nullable = false)
    private VrstaIspita vrstaIspita;

    @ManyToOne
    @JoinColumn(name = "lokacija_id", nullable = false)
    private Lokacija lokacija;

    @JsonIgnore
    @ManyToMany(mappedBy = "termini")
    private List<Prijava> prijave = new ArrayList<>();
}
