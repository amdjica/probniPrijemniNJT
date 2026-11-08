package com.projekat.backend.controller;

import com.projekat.backend.dto.VrstaIspitaDto;
import com.projekat.backend.service.VrstaIspitaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vrste-ispita")
public class VrstaIspitaController {

    private final VrstaIspitaService vrstaIspitaService;

    public VrstaIspitaController(VrstaIspitaService vrstaIspitaService) {
        this.vrstaIspitaService = vrstaIspitaService;
    }

    @GetMapping
    public List<VrstaIspitaDto> getSve() {
        return vrstaIspitaService.getSve();
    }
}
