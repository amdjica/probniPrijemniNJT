package com.projekat.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CenovnikZahtevDto(
        @NotBlank(message = "Molimo unesite naziv paketa.") String naziv,
        @NotNull(message = "Molimo unesite cenu.") @Positive(message = "Cena mora biti veća od nule.") Double cena,
        @NotNull(message = "Molimo izaberite datum od kada cena važi.") LocalDate vaziOd,
        LocalDate vaziDo,
        String opis
) {
}
