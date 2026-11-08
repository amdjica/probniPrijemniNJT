package com.projekat.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cenovnik")
@Getter
@Setter
@NoArgsConstructor
public class Cenovnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String naziv;

    @Column(nullable = false)
    private Double cena;

    @Column(nullable = false)
    private LocalDate vaziOd;

    @Column
    private LocalDate vaziDo;

    @Column(length = 1000)
    private String opis;

    @JsonIgnore
    @OneToMany(mappedBy = "cenovnik")
    private List<VrstaIspitaCenovnik> vrstaIspitaCenovnici = new ArrayList<>();
}
