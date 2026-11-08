package com.projekat.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostavljanjeLozinkeDto(
        @NotBlank(message = "Unesite email adresu.")
        @Email(message = "Uneta email adresa nije u ispravnom formatu.") String email,
        @NotBlank(message = "Unesite privremenu lozinku koju ste dobili na email.") String privremenaLozinka,
        @NotBlank(message = "Unesite novu lozinku.")
        @Size(min = 6, message = "Nova lozinka mora imati najmanje 6 karaktera.") String novaLozinka
) {
}
