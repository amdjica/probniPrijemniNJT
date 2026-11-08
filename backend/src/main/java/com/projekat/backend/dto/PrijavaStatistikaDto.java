package com.projekat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrijavaStatistikaDto {
    private Long ukupnoPrijava;
    private Map<String, Long> brojPoStatusu;
    private Map<String, Long> brojPoNacinuPlacanja;
    private Map<String, Double> ocekivaniPrihodPoVrstiIspita;
    private Map<String, Long> brojPrijavljenihPoLokaciji;
    private Long brojSaUplatnicom;
    private Long brojBezUplatnice;
    private Double prosecanBrojTerminaPoKandidatu;
    private Double ukupnoNaplaceno;
}
