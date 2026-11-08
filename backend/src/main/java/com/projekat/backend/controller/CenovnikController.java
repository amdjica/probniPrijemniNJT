package com.projekat.backend.controller;

import com.projekat.backend.dto.CenovnikDto;
import com.projekat.backend.dto.CenovnikZahtevDto;
import com.projekat.backend.service.CenovnikService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cenovnici")
public class CenovnikController {

    private final CenovnikService cenovnikService;

    public CenovnikController(CenovnikService cenovnikService) {
        this.cenovnikService = cenovnikService;
    }

    @GetMapping
    public List<CenovnikDto> getAktivne() {
        return cenovnikService.getAktivneCenovnike();
    }

    @PutMapping("/{vrstaIspitaId}")
    public CenovnikDto azuriraj(@PathVariable Long vrstaIspitaId, @Valid @RequestBody CenovnikZahtevDto zahtev) {
        return cenovnikService.azurirajCenu(vrstaIspitaId, zahtev);
    }
}
