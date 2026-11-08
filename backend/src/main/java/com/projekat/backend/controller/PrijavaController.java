package com.projekat.backend.controller;

import com.projekat.backend.dto.OdbijanjePrijaveZahtevDto;
import com.projekat.backend.dto.PrijavaDto;
import com.projekat.backend.dto.PrijavaKreiranjeZahtevDto;
import com.projekat.backend.dto.PrijavaOdlukaOdgovorDto;
import com.projekat.backend.dto.PrijavaStatistikaDto;
import com.projekat.backend.security.AutentifikovaniKorisnik;
import com.projekat.backend.service.PrijavaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/prijave")
public class PrijavaController {

    private final PrijavaService prijavaService;

    public PrijavaController(PrijavaService prijavaService) {
        this.prijavaService = prijavaService;
    }

    @PostMapping
    public PrijavaDto kreiraj(@Valid @RequestBody PrijavaKreiranjeZahtevDto zahtev, Authentication authentication) {
        AutentifikovaniKorisnik korisnik = AutentifikovaniKorisnik.zahtevaj(authentication);
        return prijavaService.kreirajPrijavu(korisnik.id(), zahtev);
    }

    @GetMapping("/moje")
    public List<PrijavaDto> getMoje(Authentication authentication) {
        AutentifikovaniKorisnik korisnik = AutentifikovaniKorisnik.zahtevaj(authentication);
        return prijavaService.getMojePrijave(korisnik.id());
    }

    @GetMapping
    public List<PrijavaDto> pretrazi(@RequestParam(required = false) String ime,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) Long vrstaIspitaId,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate datumTermina,
                                      @RequestParam(required = false) String status) {
        return prijavaService.pretraziPrijave(ime, email, vrstaIspitaId, datumTermina, status);
    }

    @GetMapping("/statistika")
    public PrijavaStatistikaDto statistika() {
        return prijavaService.getStatistika();
    }

    @GetMapping("/{id}")
    public PrijavaDto getDetalji(@PathVariable Long id, Authentication authentication) {
        AutentifikovaniKorisnik korisnik = AutentifikovaniKorisnik.zahtevaj(authentication);
        Long kandidatId = "KANDIDAT".equals(korisnik.uloga()) ? korisnik.id() : null;
        return prijavaService.getDetalji(id, kandidatId);
    }

    @PutMapping("/{id}/odobri")
    public PrijavaOdlukaOdgovorDto odobri(@PathVariable Long id, Authentication authentication) {
        AutentifikovaniKorisnik korisnik = AutentifikovaniKorisnik.zahtevaj(authentication);
        return prijavaService.odobriPrijavu(korisnik.id(), id);
    }

    @PutMapping("/{id}/odbij")
    public PrijavaOdlukaOdgovorDto odbij(@PathVariable Long id,
                                          @Valid @RequestBody OdbijanjePrijaveZahtevDto zahtev,
                                          Authentication authentication) {
        AutentifikovaniKorisnik korisnik = AutentifikovaniKorisnik.zahtevaj(authentication);
        return prijavaService.odbijPrijavu(korisnik.id(), id, zahtev.napomena());
    }
}
