package com.projekat.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PokretanjeRegistracijeDto(
        @NotBlank(message = "Molimo unesite svoje ime.") String ime,
        @NotBlank(message = "Molimo unesite svoje prezime.") String prezime,
        String telefon,
        @NotBlank(message = "Molimo unesite email adresu.")
        @Email(message = "Uneta email adresa nije u ispravnom formatu.") String email
) {
}
