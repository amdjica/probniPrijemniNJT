package com.projekat.backend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PrijavaKreiranjeZahtevDto(
        @NotEmpty(message = "Izaberite bar jedan termin pre slanja prijave.") List<Long> terminiId,
        @NotNull(message = "Izaberite način plaćanja.") NacinPlacanja nacinPlacanja
) {
}
