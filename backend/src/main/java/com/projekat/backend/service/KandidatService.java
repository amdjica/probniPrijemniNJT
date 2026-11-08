package com.projekat.backend.service;

import com.projekat.backend.dto.AuthOdgovorDto;
import com.projekat.backend.dto.LoginZahtevDto;
import com.projekat.backend.dto.PokretanjeRegistracijeDto;
import com.projekat.backend.dto.PorukaOdgovorDto;
import com.projekat.backend.dto.PostavljanjeLozinkeDto;
import com.projekat.backend.dto.ProveraPrivremeneLozinkeDto;
import com.projekat.backend.entity.Kandidat;
import com.projekat.backend.repository.KandidatRepository;
import com.projekat.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registracija kandidata se odvija u tri koraka. Kandidat NEMA lozinku dok ne prođe kroz sva tri koraka,
 * pa se ništa ne upisuje u bazu (entitet Kandidat) dok korak 3 ne postavi trajnu lozinku.
 * Privremena lozinka i osnovni podaci iz koraka 1 se drže isključivo u ovoj in-memory mapi po email adresi -
 * nikad kao kolona u bazi i nikad kao atribut entiteta.
 */
@Service
public class KandidatService {

    private static final String ALFABET_PRIVREMENE_LOZINKE = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final int DUZINA_PRIVREMENE_LOZINKE = 10;
    private static final long TRAJANJE_PRIVREMENE_LOZINKE_MINUTA = 30;

    private final KandidatRepository kandidatRepository;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final SecureRandom generatorSlucajnosti = new SecureRandom();

    private final Map<String, RegistracijaUToku> registracijeUToku = new ConcurrentHashMap<>();

    public KandidatService(KandidatRepository kandidatRepository,
                            JwtService jwtService,
                            JavaMailSender mailSender) {
        this.kandidatRepository = kandidatRepository;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
    }

    public PorukaOdgovorDto pokreniRegistraciju(PokretanjeRegistracijeDto zahtev) {
        if (kandidatRepository.existsByEmail(zahtev.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Nalog sa ovom email adresom je već registrovan. Prijavite se umesto toga.");
        }

        String privremenaLozinka = generisiPrivremenuLozinku();
        RegistracijaUToku unos = new RegistracijaUToku(
                zahtev.ime(), zahtev.prezime(), zahtev.telefon(), zahtev.email(),
                privremenaLozinka, LocalDateTime.now().plusMinutes(TRAJANJE_PRIVREMENE_LOZINKE_MINUTA));
        registracijeUToku.put(zahtev.email(), unos);

        posaljiPrivremenuLozinku(unos);

        return new PorukaOdgovorDto("Privremena lozinka je poslata na unetu email adresu. Proverite prijemno sanduče i nastavite registraciju.");
    }

    public PorukaOdgovorDto potvrdiPrivremenuLozinku(ProveraPrivremeneLozinkeDto zahtev) {
        pronadjiIProveriRegistraciju(zahtev.email(), zahtev.privremenaLozinka());
        return new PorukaOdgovorDto("Privremena lozinka je ispravna. Sada možete postaviti svoju trajnu lozinku.");
    }

    @Transactional
    public PorukaOdgovorDto postaviTrajnuLozinku(PostavljanjeLozinkeDto zahtev) {
        RegistracijaUToku unos = pronadjiIProveriRegistraciju(zahtev.email(), zahtev.privremenaLozinka());

        Kandidat kandidat = new Kandidat();
        kandidat.setIme(unos.ime);
        kandidat.setPrezime(unos.prezime);
        kandidat.setTelefon(unos.telefon);
        kandidat.setEmail(unos.email);
        kandidat.setUsername(unos.email);
        kandidat.setPassword(zahtev.novaLozinka());
        kandidatRepository.save(kandidat);

        registracijeUToku.remove(zahtev.email());

        return new PorukaOdgovorDto("Registracija je uspešno završena. Sada se možete prijaviti na sistem svojom novom lozinkom.");
    }

    public AuthOdgovorDto prijaviSe(LoginZahtevDto zahtev) {
        Kandidat kandidat = kandidatRepository.findByEmail(zahtev.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Nalog sa unetom email adresom ne postoji ili registracija još uvek nije završena."));

        if (!zahtev.password().equals(kandidat.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Uneta lozinka nije ispravna. Proverite podatke i pokušajte ponovo.");
        }

        String token = jwtService.generisiToken(kandidat.getId(), kandidat.getUsername(), "KANDIDAT");
        return new AuthOdgovorDto(kandidat.getId(), kandidat.getIme(), kandidat.getPrezime(),
                kandidat.getUsername(), kandidat.getEmail(), "KANDIDAT", token);
    }

    public Kandidat pronadjiPoId(Long id) {
        return kandidatRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nalog kandidata nije pronađen."));
    }

    private RegistracijaUToku pronadjiIProveriRegistraciju(String email, String unetaPrivremenaLozinka) {
        RegistracijaUToku unos = registracijeUToku.get(email);
        if (unos == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nije pronađena registracija u toku za unetu email adresu.");
        }

        if (unos.privremenaLozinkaIstice.isBefore(LocalDateTime.now())) {
            registracijeUToku.remove(email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Privremena lozinka je istekla. Pokrenite registraciju ponovo.");
        }

        if (!unos.privremenaLozinka.equals(unetaPrivremenaLozinka)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Privremena lozinka nije ispravna.");
        }

        return unos;
    }

    private String generisiPrivremenuLozinku() {
        StringBuilder lozinka = new StringBuilder(DUZINA_PRIVREMENE_LOZINKE);
        for (int i = 0; i < DUZINA_PRIVREMENE_LOZINKE; i++) {
            lozinka.append(ALFABET_PRIVREMENE_LOZINKE.charAt(generatorSlucajnosti.nextInt(ALFABET_PRIVREMENE_LOZINKE.length())));
        }
        return lozinka.toString();
    }

    private void posaljiPrivremenuLozinku(RegistracijaUToku unos) {
        try {
            SimpleMailMessage poruka = new SimpleMailMessage();
            poruka.setTo(unos.email);
            poruka.setSubject("Privremena lozinka za probni prijemni");
            poruka.setText("Poštovani/a " + unos.ime + " " + unos.prezime + ",\n\n"
                    + "Započeli ste registraciju za sistem prijave na probni prijemni ispit.\n\n"
                    + "Vaša privremena lozinka je:\n\n"
                    + unos.privremenaLozinka + "\n\n"
                    + "Unesite ovu lozinku u aplikaciji kako biste nastavili registraciju. Nakon toga ćete postaviti "
                    + "novu lozinku koju ćete koristiti za svako naredno prijavljivanje. Privremena lozinka važi "
                    + TRAJANJE_PRIVREMENE_LOZINKE_MINUTA + " minuta.\n\n"
                    + "Ukoliko niste vi započeli registraciju, možete slobodno da ignorišete ovu poruku.\n\n"
                    + "Srdačno,\nTim za probni prijemni");
            mailSender.send(poruka);
        } catch (MailException ex) {
            // Neuspelo slanje emaila ne sme da poništi već generisanu privremenu lozinku - kandidat može zatražiti novu.
        }
    }

    /** Isključivo privremena, in-memory struktura - nikad entitet, nikad repository, nikad tabela u bazi. */
    private static final class RegistracijaUToku {
        private final String ime;
        private final String prezime;
        private final String telefon;
        private final String email;
        private final String privremenaLozinka;
        private final LocalDateTime privremenaLozinkaIstice;

        private RegistracijaUToku(String ime, String prezime, String telefon, String email,
                                   String privremenaLozinka, LocalDateTime privremenaLozinkaIstice) {
            this.ime = ime;
            this.prezime = prezime;
            this.telefon = telefon;
            this.email = email;
            this.privremenaLozinka = privremenaLozinka;
            this.privremenaLozinkaIstice = privremenaLozinkaIstice;
        }
    }
}
