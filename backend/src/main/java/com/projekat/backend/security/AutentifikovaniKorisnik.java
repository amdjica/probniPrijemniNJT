package com.projekat.backend.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

public record AutentifikovaniKorisnik(Long id, String username, String uloga) {

    public static AutentifikovaniKorisnik iz(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof AutentifikovaniKorisnik korisnik) {
            return korisnik;
        }
        return null;
    }

    public static AutentifikovaniKorisnik zahtevaj(Authentication authentication) {
        AutentifikovaniKorisnik korisnik = iz(authentication);
        if (korisnik == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Niste prijavljeni.");
        }
        return korisnik;
    }
}
