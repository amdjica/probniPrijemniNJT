package com.projekat.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProveraPrivremeneLozinkeDto(
        @NotBlank(message = "Unesite email adresu.")
        @Email(message = "Uneta email adresa nije u ispravnom formatu.") String email,
        @NotBlank(message = "Unesite privremenu lozinku koju ste dobili na email.") String privremenaLozinka
) {
}
