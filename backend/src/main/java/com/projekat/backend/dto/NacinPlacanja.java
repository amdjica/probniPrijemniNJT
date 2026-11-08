package com.projekat.backend.dto;

/**
 * Ne postoji kao entity atribut (Prijava u modelu nema polje za način plaćanja).
 * Koristi se samo u DTO/service sloju - vrednost se čuva kodirana unutar postojećeg
 * Prijava.napomena polja (videti PrijavaService).
 */
public enum NacinPlacanja {
    JEDNOKRATNO,
    RATE
}
