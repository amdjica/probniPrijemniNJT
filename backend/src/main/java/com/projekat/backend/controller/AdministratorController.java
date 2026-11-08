package com.projekat.backend.controller;

import com.projekat.backend.dto.AuthOdgovorDto;
import com.projekat.backend.dto.LoginZahtevDto;
import com.projekat.backend.service.AdministratorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nalozi/administratori")
public class AdministratorController {

    private final AdministratorService administratorService;

    public AdministratorController(AdministratorService administratorService) {
        this.administratorService = administratorService;
    }

    @PostMapping("/prijavljivanje")
    public AuthOdgovorDto prijaviSe(@Valid @RequestBody LoginZahtevDto zahtev) {
        return administratorService.prijaviSe(zahtev);
    }
}
