package com.projekat.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TerminZahtevDto(
        @NotNull(message = "Molimo izaberite datum termina.") LocalDate datum,
        @NotNull(message = "Molimo unesite vreme početka.") String vremePocetka,
        String napomena,
        Boolean dostupan,
        @NotNull(message = "Molimo izaberite vrstu ispita.") Long vrstaIspitaId,
        @NotNull(message = "Molimo izaberite lokaciju održavanja.") Long lokacijaId
) {
}
