package com.projekat.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prijava")
@Getter
@Setter
@NoArgsConstructor
public class Prijava {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate datumPrijave;

    @Column(nullable = false)
    private Double cena;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPrijave status = StatusPrijave.NA_CEKANJU;

    @Column(length = 500)
    private String napomena;

    @ManyToOne
    @JoinColumn(name = "kandidat_id", nullable = false)
    private Kandidat kandidat;

    @ManyToOne
    @JoinColumn(name = "administrator_id", nullable = true)
    private Administrator administrator;

    @ManyToMany
    @JoinTable(
            name = "prijava_termin",
            joinColumns = @JoinColumn(name = "prijava_id"),
            inverseJoinColumns = @JoinColumn(name = "termin_id")
    )
    @JsonIgnore
    private List<Termin> termini = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "prijava")
    private List<Uplata> uplate = new ArrayList<>();
}
