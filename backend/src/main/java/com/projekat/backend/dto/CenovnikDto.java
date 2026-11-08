package com.projekat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CenovnikDto {
    private Long id;
    private String naziv;
    private Double cena;
    private LocalDate vaziOd;
    private LocalDate vaziDo;
    private String opis;
    private Long vrstaIspitaId;
    private String vrstaIspitaNaziv;
    private Boolean aktivna;
}
