package com.projekat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrijavaDto {
    private Long id;
    private LocalDate datumPrijave;
    private String status;
    private Double cena;
    private Double uplaceno;
    private Double preostalo;
    private String nacinPlacanja;
    private Integer brojRata;
    private Double iznosJedneRate;
    private String napomena;
    private Boolean mozeDaDodaUplatu;
    private Boolean mozeDaSeOdobri;
    private Boolean mozeDaSeOdbije;
    private Long kandidatId;
    private String kandidatIme;
    private String kandidatPrezime;
    private String kandidatEmail;
    private List<TerminDto> termini = new ArrayList<>();
    private List<UplataDto> uplate = new ArrayList<>();
}
