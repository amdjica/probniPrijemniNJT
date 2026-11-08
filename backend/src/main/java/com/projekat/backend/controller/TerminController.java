package com.projekat.backend.controller;

import com.projekat.backend.dto.KandidatZaTerminDto;
import com.projekat.backend.dto.TerminDto;
import com.projekat.backend.dto.TerminZahtevDto;
import com.projekat.backend.security.AutentifikovaniKorisnik;
import com.projekat.backend.service.TerminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/termini")
public class TerminController {

    private final TerminService terminService;

    public TerminController(TerminService terminService) {
        this.terminService = terminService;
    }

    @GetMapping
    public List<TerminDto> getSvi(@RequestParam(required = false) Long vrstaIspitaId, Authentication authentication) {
        AutentifikovaniKorisnik korisnik = AutentifikovaniKorisnik.iz(authentication);
        Long kandidatId = (korisnik != null && "KANDIDAT".equals(korisnik.uloga())) ? korisnik.id() : null;
        return terminService.getSvi(vrstaIspitaId, kandidatId);
    }

    @PostMapping
    public TerminDto kreiraj(@Valid @RequestBody TerminZahtevDto zahtev) {
        return terminService.kreiraj(zahtev);
    }

    @PutMapping("/{id}")
    public TerminDto azuriraj(@PathVariable Long id, @Valid @RequestBody TerminZahtevDto zahtev) {
        return terminService.azuriraj(id, zahtev);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> obrisi(@PathVariable Long id) {
        terminService.obrisi(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/prijavljeni-kandidati")
    public List<KandidatZaTerminDto> getPrijavljeneKandidate(@PathVariable Long id) {
        return terminService.getPrijavljeneKandidate(id);
    }

    @GetMapping("/{id}/izvestaj")
    public ResponseEntity<byte[]> preuzmiIzvestaj(@PathVariable Long id) {
        byte[] pdf = terminService.generisiIzvestajKandidata(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"termin-" + id + "-kandidati.pdf\"")
                .body(pdf);
    }
}
