package com.projekat.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record OdbijanjePrijaveZahtevDto(
        @NotBlank(message = "Molimo unesite obrazloženje odbijanja pre potvrde.") String napomena
) {
}
