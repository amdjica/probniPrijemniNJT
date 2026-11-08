package com.projekat.backend.service;

import com.projekat.backend.dto.PrijavaOdlukaOdgovorDto;
import com.projekat.backend.entity.Administrator;
import com.projekat.backend.entity.Kandidat;
import com.projekat.backend.entity.Prijava;
import com.projekat.backend.entity.StatusPrijave;
import com.projekat.backend.entity.Uplata;
import com.projekat.backend.repository.PrijavaRepository;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit testovi za odobravanje i odbijanje prijava.
 * Proveravaju poslovna pravila: prijava se odobrava tek kada je uplaćen pun iznos,
 * a odobrena prijava se ne može ponovo odobriti niti odbiti.
 */
@ExtendWith(MockitoExtension.class)
class PrijavaServiceTest {

    private static final Long ADMIN_ID = 10L;
    private static final Long PRIJAVA_ID = 1L;

    @Mock private PrijavaRepository prijavaRepository;
    @Mock private TerminService terminService;
    @Mock private KandidatService kandidatService;
    @Mock private AdministratorService administratorService;
    @Mock private UplataService uplataService;
    @Mock private JavaMailSender mailSender;

    private PrijavaService prijavaService;

    @BeforeEach
    void setUp() {
        prijavaService = new PrijavaService(prijavaRepository, terminService, kandidatService,
                administratorService, uplataService, mailSender);
    }

    @Test
    @DisplayName("Prijava se odobrava kada je uplaćen pun iznos, a kandidat dobija email")
    void odobriPrijavu_punoUplaceno_odobravaISaljeEmail() {
        Prijava prijava = prijava(3000.0, StatusPrijave.NA_CEKANJU, 1000.0, 2000.0);
        Administrator admin = new Administrator();
        admin.setId(ADMIN_ID);
        when(prijavaRepository.findById(PRIJAVA_ID)).thenReturn(Optional.of(prijava));
        when(administratorService.pronadjiPoId(ADMIN_ID)).thenReturn(admin);
        when(prijavaRepository.save(any(Prijava.class))).thenAnswer(poziv -> poziv.getArgument(0));

        PrijavaOdlukaOdgovorDto odgovor = prijavaService.odobriPrijavu(ADMIN_ID, PRIJAVA_ID);

        assertEquals(StatusPrijave.ODOBRENA, prijava.getStatus());
        assertSame(admin, prijava.getAdministrator());
        assertEquals("ODOBRENA", odgovor.prijava().getStatus());
        assertTrue(odgovor.obavestenjePoslato());

        ArgumentCaptor<SimpleMailMessage> poruka = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(poruka.capture());
        assertEquals("pera@example.com", poruka.getValue().getTo()[0]);
    }

    @Test
    @DisplayName("Prijava se ne može odobriti ako uplaćeni iznos nije jednak ceni")
    void odobriPrijavu_nedovoljnoUplaceno_bacaBadRequest() {
        Prijava prijava = prijava(3000.0, StatusPrijave.NA_CEKANJU, 1000.0);
        when(prijavaRepository.findById(PRIJAVA_ID)).thenReturn(Optional.of(prijava));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> prijavaService.odobriPrijavu(ADMIN_ID, PRIJAVA_ID));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals(StatusPrijave.NA_CEKANJU, prijava.getStatus());
        verify(prijavaRepository, never()).save(any(Prijava.class));
    }

    @Test
    @DisplayName("Već odobrena prijava se ne može ponovo odobriti")
    void odobriPrijavu_vecOdobrena_bacaConflict() {
        Prijava prijava = prijava(3000.0, StatusPrijave.ODOBRENA, 3000.0);
        when(prijavaRepository.findById(PRIJAVA_ID)).thenReturn(Optional.of(prijava));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> prijavaService.odobriPrijavu(ADMIN_ID, PRIJAVA_ID));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Nepostojeća prijava vraća 404")
    void odobriPrijavu_nepostojecaPrijava_bacaNotFound() {
        when(prijavaRepository.findById(PRIJAVA_ID)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> prijavaService.odobriPrijavu(ADMIN_ID, PRIJAVA_ID));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("Odbijanje prijave menja status i čuva napomenu administratora")
    void odbijPrijavu_naCekanju_odbijaICuvaNapomenu() {
        Prijava prijava = prijava(3000.0, StatusPrijave.NA_CEKANJU);
        when(prijavaRepository.findById(PRIJAVA_ID)).thenReturn(Optional.of(prijava));
        when(prijavaRepository.save(any(Prijava.class))).thenAnswer(poziv -> poziv.getArgument(0));

        PrijavaOdlukaOdgovorDto odgovor = prijavaService.odbijPrijavu(ADMIN_ID, PRIJAVA_ID, "Uplatnica nije čitljiva.");

        assertEquals(StatusPrijave.ODBIJENA, prijava.getStatus());
        assertEquals("ODBIJENA", odgovor.prijava().getStatus());
        assertEquals("Uplatnica nije čitljiva.", odgovor.prijava().getNapomena());
    }

    @Test
    @DisplayName("Odobrena prijava se ne može naknadno odbiti")
    void odbijPrijavu_vecOdobrena_bacaConflict() {
        Prijava prijava = prijava(3000.0, StatusPrijave.ODOBRENA, 3000.0);
        when(prijavaRepository.findById(PRIJAVA_ID)).thenReturn(Optional.of(prijava));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> prijavaService.odbijPrijavu(ADMIN_ID, PRIJAVA_ID, "Razlog"));

        assertEquals(409, ex.getStatusCode().value());
        verify(prijavaRepository, never()).save(any(Prijava.class));
    }

    @Test
    @DisplayName("Ako slanje emaila ne uspe, odluka ostaje sačuvana, a odgovor to označava")
    void odbijPrijavu_greskaPriSlanjuEmaila_odlukaSeIpakCuva() {
        Prijava prijava = prijava(3000.0, StatusPrijave.NA_CEKANJU);
        when(prijavaRepository.findById(PRIJAVA_ID)).thenReturn(Optional.of(prijava));
        when(prijavaRepository.save(any(Prijava.class))).thenAnswer(poziv -> poziv.getArgument(0));
        doThrow(new MailSendException("SMTP nije dostupan")).when(mailSender).send(any(SimpleMailMessage.class));

        PrijavaOdlukaOdgovorDto odgovor = prijavaService.odbijPrijavu(ADMIN_ID, PRIJAVA_ID, "Razlog");

        assertEquals(StatusPrijave.ODBIJENA, prijava.getStatus());
        assertFalse(odgovor.obavestenjePoslato());
    }

    private Prijava prijava(double cena, StatusPrijave status, double... iznosiUplata) {
        Kandidat kandidat = new Kandidat();
        kandidat.setId(5L);
        kandidat.setIme("Pera");
        kandidat.setPrezime("Perić");
        kandidat.setEmail("pera@example.com");

        Prijava prijava = new Prijava();
        prijava.setId(PRIJAVA_ID);
        prijava.setDatumPrijave(LocalDate.now());
        prijava.setCena(cena);
        prijava.setStatus(status);
        prijava.setKandidat(kandidat);

        long id = 100L;
        for (double iznos : iznosiUplata) {
            Uplata uplata = new Uplata();
            uplata.setId(id++);
            uplata.setIznos(iznos);
            uplata.setDatumUplate(LocalDate.now());
            uplata.setPrijava(prijava);
            prijava.getUplate().add(uplata);
        }
        return prijava;
    }
}
