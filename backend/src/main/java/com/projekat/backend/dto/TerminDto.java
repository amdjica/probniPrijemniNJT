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
public class TerminDto {
    private Long id;
    private LocalDate datum;
    private String vremePocetka;
    private String napomena;
    private Boolean dostupan;
    private Long vrstaIspitaId;
    private String vrstaIspitaNaziv;
    private Long lokacijaId;
    private String lokacijaNaziv;
    private String lokacijaAdresa;
    private Double cena;
    private Integer brojPrijava;
    private Boolean mozeDaSeMenja;
    private Boolean mozeDaSeObrise;
}
