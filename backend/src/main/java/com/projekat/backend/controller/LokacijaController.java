package com.projekat.backend.controller;

import com.projekat.backend.dto.LokacijaDto;
import com.projekat.backend.service.LokacijaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lokacije")
public class LokacijaController {

    private final LokacijaService lokacijaService;

    public LokacijaController(LokacijaService lokacijaService) {
        this.lokacijaService = lokacijaService;
    }

    @GetMapping
    public List<LokacijaDto> getSve() {
        return lokacijaService.getSve();
    }
}
