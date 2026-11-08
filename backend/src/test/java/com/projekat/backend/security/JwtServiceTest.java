package com.projekat.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit testovi za generisanje i proveru JWT tokena.
 */
class JwtServiceTest {

    private static final String KLJUC = "test-secret-key-not-for-production-use-0123456789abcdef";

    private final JwtService jwtService = new JwtService(KLJUC, 3_600_000);

    @Test
    @DisplayName("Token sadrži korisničko ime, id i ulogu")
    void generisiToken_sadrziPodatkeKorisnika() {
        String token = jwtService.generisiToken(7L, "pera@example.com", "KANDIDAT");

        Claims podaci = jwtService.raspakujToken(token);

        assertEquals("pera@example.com", podaci.getSubject());
        assertEquals("7", podaci.get("id", String.class));
        assertEquals("KANDIDAT", podaci.get("uloga", String.class));
    }

    @Test
    @DisplayName("Izmenjen token se odbija")
    void raspakujToken_izmenjenToken_bacaIzuzetak() {
        String token = jwtService.generisiToken(7L, "pera@example.com", "KANDIDAT");
        // Menjamo prvi znak potpisa (deo posle poslednje tačke), pa potpis više ne odgovara sadržaju
        int pocetakPotpisa = token.lastIndexOf('.') + 1;
        char prviZnak = token.charAt(pocetakPotpisa);
        char zamena = prviZnak == 'A' ? 'B' : 'A';
        String izmenjen = token.substring(0, pocetakPotpisa) + zamena + token.substring(pocetakPotpisa + 1);

        assertThrows(JwtException.class, () -> jwtService.raspakujToken(izmenjen));
    }

    @Test
    @DisplayName("Token potpisan drugim ključem se odbija")
    void raspakujToken_drugiKljuc_bacaIzuzetak() {
        JwtService drugiServis = new JwtService("sasvim-drugi-tajni-kljuc-za-test-0123456789abcdefgh", 3_600_000);
        String tudjiToken = drugiServis.generisiToken(7L, "pera@example.com", "ADMINISTRATOR");

        assertThrows(JwtException.class, () -> jwtService.raspakujToken(tudjiToken));
    }

    @Test
    @DisplayName("Istekao token se odbija")
    void raspakujToken_istekaoToken_bacaIzuzetak() {
        JwtService kratkoTrajanje = new JwtService(KLJUC, -1_000);
        String istekao = kratkoTrajanje.generisiToken(7L, "pera@example.com", "KANDIDAT");

        assertThrows(JwtException.class, () -> jwtService.raspakujToken(istekao));
    }
}
