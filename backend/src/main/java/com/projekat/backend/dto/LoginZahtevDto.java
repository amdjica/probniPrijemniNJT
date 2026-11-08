package com.projekat.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginZahtevDto(
        @NotBlank(message = "Unesite email adresu.")
        @Email(message = "Uneta email adresa nije u ispravnom formatu.") String email,
        @NotBlank(message = "Unesite lozinku.") String password
) {
}
