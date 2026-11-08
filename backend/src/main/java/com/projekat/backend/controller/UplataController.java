package com.projekat.backend.controller;

import com.projekat.backend.dto.UplataDto;
import com.projekat.backend.security.AutentifikovaniKorisnik;
import com.projekat.backend.service.UplataService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/prijave/{prijavaId}/uplate")
public class UplataController {

    private final UplataService uplataService;

    public UplataController(UplataService uplataService) {
        this.uplataService = uplataService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public UplataDto dodaj(@PathVariable Long prijavaId,
                            @RequestParam Double iznos,
                            @RequestParam(required = false) LocalDate datumUplate,
                            @RequestParam("uplatnica") MultipartFile uplatnica,
                            Authentication authentication) {
        AutentifikovaniKorisnik korisnik = AutentifikovaniKorisnik.zahtevaj(authentication);
        return uplataService.dodajUplatu(korisnik.id(), prijavaId, iznos, datumUplate, uplatnica);
    }

    @GetMapping
    public List<UplataDto> getSve(@PathVariable Long prijavaId, Authentication authentication) {
        AutentifikovaniKorisnik korisnik = AutentifikovaniKorisnik.zahtevaj(authentication);
        Long kandidatId = "KANDIDAT".equals(korisnik.uloga()) ? korisnik.id() : null;
        return uplataService.getUplateZaPrijavu(prijavaId, kandidatId);
    }

    @GetMapping("/{uplataId}/uplatnica")
    public ResponseEntity<Resource> preuzmiUplatnicu(@PathVariable Long prijavaId,
                                                       @PathVariable Long uplataId,
                                                       Authentication authentication) {
        AutentifikovaniKorisnik korisnik = AutentifikovaniKorisnik.zahtevaj(authentication);
        Long kandidatId = "KANDIDAT".equals(korisnik.uloga()) ? korisnik.id() : null;
        Resource fajl = uplataService.getUplatnica(prijavaId, uplataId, kandidatId);
        MediaType tipSadrzaja = MediaTypeFactory.getMediaType(fajl).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(tipSadrzaja)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fajl.getFilename() + "\"")
                .body(fajl);
    }
}
