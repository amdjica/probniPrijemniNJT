package com.projekat.backend.service;

import com.projekat.backend.dto.AuthOdgovorDto;
import com.projekat.backend.dto.LoginZahtevDto;
import com.projekat.backend.dto.PokretanjeRegistracijeDto;
import com.projekat.backend.dto.PostavljanjeLozinkeDto;
import com.projekat.backend.dto.ProveraPrivremeneLozinkeDto;
import com.projekat.backend.entity.Kandidat;
import com.projekat.backend.repository.KandidatRepository;
import com.projekat.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit testovi za registraciju i prijavljivanje kandidata.
 * Baza (repository) i slanje emaila su zamenjeni Mockito mock objektima, pa testovi ne zahtevaju MySQL ni mail server.
 */
@ExtendWith(MockitoExtension.class)
class KandidatServiceTest {

    private static final String EMAIL = "pera@example.com";

    @Mock
    private KandidatRepository kandidatRepository;

    @Mock
    private JavaMailSender mailSender;

    private KandidatService kandidatService;

    @BeforeEach
    void setUp() {
        JwtService jwtService = new JwtService("test-secret-key-not-for-production-use-0123456789abcdef", 3_600_000);
        kandidatService = new KandidatService(kandidatRepository, jwtService, mailSender);
    }

    @Test
    @DisplayName("Registracija se odbija ako nalog sa istim emailom već postoji")
    void pokreniRegistraciju_postojeciEmail_bacaConflict() {
        when(kandidatRepository.existsByEmail(EMAIL)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> kandidatService.pokreniRegistraciju(new PokretanjeRegistracijeDto("Pera", "Perić", null, EMAIL)));

        assertEquals(409, ex.getStatusCode().value());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Kompletna registracija u tri koraka čuva kandidata sa unetim podacima")
    void registracija_triKoraka_cuvaKandidata() {
        when(kandidatRepository.existsByEmail(EMAIL)).thenReturn(false);

        // Korak 1: pokretanje registracije šalje email sa privremenom lozinkom
        kandidatService.pokreniRegistraciju(new PokretanjeRegistracijeDto("Pera", "Perić", "0641234567", EMAIL));
        String privremenaLozinka = procitajPrivremenuLozinkuIzEmaila();

        // Korak 2: provera privremene lozinke
        assertDoesNotThrow(() -> kandidatService.potvrdiPrivremenuLozinku(
                new ProveraPrivremeneLozinkeDto(EMAIL, privremenaLozinka)));

        // Korak 3: postavljanje trajne lozinke upisuje kandidata u bazu
        kandidatService.postaviTrajnuLozinku(new PostavljanjeLozinkeDto(EMAIL, privremenaLozinka, "novaLozinka123"));

        ArgumentCaptor<Kandidat> sacuvan = ArgumentCaptor.forClass(Kandidat.class);
        verify(kandidatRepository).save(sacuvan.capture());
        assertEquals("Pera", sacuvan.getValue().getIme());
        assertEquals("Perić", sacuvan.getValue().getPrezime());
        assertEquals(EMAIL, sacuvan.getValue().getEmail());
        assertEquals(EMAIL, sacuvan.getValue().getUsername());
        assertEquals("novaLozinka123", sacuvan.getValue().getPassword());
    }

    @Test
    @DisplayName("Pogrešna privremena lozinka se odbija")
    void potvrdiPrivremenuLozinku_pogresnaLozinka_bacaUnauthorized() {
        when(kandidatRepository.existsByEmail(EMAIL)).thenReturn(false);
        kandidatService.pokreniRegistraciju(new PokretanjeRegistracijeDto("Pera", "Perić", null, EMAIL));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> kandidatService.potvrdiPrivremenuLozinku(new ProveraPrivremeneLozinkeDto(EMAIL, "pogresna")));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Provera lozinke bez pokrenute registracije vraća 404")
    void potvrdiPrivremenuLozinku_bezRegistracije_bacaNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> kandidatService.potvrdiPrivremenuLozinku(new ProveraPrivremeneLozinkeDto(EMAIL, "bilosta")));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Neuspelo slanje emaila ne prekida registraciju")
    void pokreniRegistraciju_greskaPriSlanjuEmaila_neBacaIzuzetak() {
        when(kandidatRepository.existsByEmail(EMAIL)).thenReturn(false);
        doThrow(new MailSendException("SMTP nije dostupan")).when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> kandidatService.pokreniRegistraciju(
                new PokretanjeRegistracijeDto("Pera", "Perić", null, EMAIL)));
    }

    @Test
    @DisplayName("Prijava sa ispravnom lozinkom vraća JWT token i ulogu KANDIDAT")
    void prijaviSe_ispravnaLozinka_vracaToken() {
        when(kandidatRepository.findByEmail(EMAIL)).thenReturn(Optional.of(kandidat("tajna123")));

        AuthOdgovorDto odgovor = kandidatService.prijaviSe(new LoginZahtevDto(EMAIL, "tajna123"));

        assertEquals("KANDIDAT", odgovor.getTipNaloga());
        assertEquals(EMAIL, odgovor.getEmail());
        assertNotNull(odgovor.getToken());
    }

    @Test
    @DisplayName("Prijava sa pogrešnom lozinkom se odbija")
    void prijaviSe_pogresnaLozinka_bacaUnauthorized() {
        when(kandidatRepository.findByEmail(EMAIL)).thenReturn(Optional.of(kandidat("tajna123")));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> kandidatService.prijaviSe(new LoginZahtevDto(EMAIL, "pogresna")));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Prijava sa nepostojećim emailom se odbija")
    void prijaviSe_nepostojeciEmail_bacaUnauthorized() {
        when(kandidatRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> kandidatService.prijaviSe(new LoginZahtevDto(EMAIL, "bilosta")));

        assertEquals(401, ex.getStatusCode().value());
    }

    private String procitajPrivremenuLozinkuIzEmaila() {
        ArgumentCaptor<SimpleMailMessage> poruka = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(poruka.capture());
        String tekst = poruka.getValue().getText();
        assertNotNull(tekst);
        // Lozinka je u emailu u posebnom redu, odmah posle reda "Vaša privremena lozinka je:"
        String posleNaslova = tekst.split("Vaša privremena lozinka je:")[1].trim();
        return posleNaslova.split("\\s+")[0];
    }

    private Kandidat kandidat(String lozinka) {
        Kandidat k = new Kandidat();
        k.setId(1L);
        k.setIme("Pera");
        k.setPrezime("Perić");
        k.setEmail(EMAIL);
        k.setUsername(EMAIL);
        k.setPassword(lozinka);
        return k;
    }
}
