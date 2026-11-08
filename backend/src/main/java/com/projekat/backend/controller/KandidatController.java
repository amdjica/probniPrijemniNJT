package com.projekat.backend.controller;

import com.projekat.backend.dto.AuthOdgovorDto;
import com.projekat.backend.dto.LoginZahtevDto;
import com.projekat.backend.dto.PokretanjeRegistracijeDto;
import com.projekat.backend.dto.PorukaOdgovorDto;
import com.projekat.backend.dto.PostavljanjeLozinkeDto;
import com.projekat.backend.dto.ProveraPrivremeneLozinkeDto;
import com.projekat.backend.service.KandidatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nalozi/kandidati")
public class KandidatController {

    private final KandidatService kandidatService;

    public KandidatController(KandidatService kandidatService) {
        this.kandidatService = kandidatService;
    }

    @PostMapping("/registracija/pocni")
    public PorukaOdgovorDto pokreniRegistraciju(@Valid @RequestBody PokretanjeRegistracijeDto zahtev) {
        return kandidatService.pokreniRegistraciju(zahtev);
    }

    @PostMapping("/registracija/potvrdi")
    public PorukaOdgovorDto potvrdiPrivremenuLozinku(@Valid @RequestBody ProveraPrivremeneLozinkeDto zahtev) {
        return kandidatService.potvrdiPrivremenuLozinku(zahtev);
    }

    @PostMapping("/registracija/postavi-lozinku")
    public PorukaOdgovorDto postaviTrajnuLozinku(@Valid @RequestBody PostavljanjeLozinkeDto zahtev) {
        return kandidatService.postaviTrajnuLozinku(zahtev);
    }

    @PostMapping("/prijavljivanje")
    public AuthOdgovorDto prijaviSe(@Valid @RequestBody LoginZahtevDto zahtev) {
        return kandidatService.prijaviSe(zahtev);
    }
}
