package com.projekat.backend.service;

import com.projekat.backend.dto.VrstaIspitaDto;
import com.projekat.backend.entity.VrstaIspita;
import com.projekat.backend.repository.VrstaIspitaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VrstaIspitaService {

    private final VrstaIspitaRepository vrstaIspitaRepository;

    public VrstaIspitaService(VrstaIspitaRepository vrstaIspitaRepository) {
        this.vrstaIspitaRepository = vrstaIspitaRepository;
    }

    public List<VrstaIspitaDto> getSve() {
        return vrstaIspitaRepository.findAll().stream()
                .map(v -> new VrstaIspitaDto(v.getId(), v.getNaziv()))
                .toList();
    }

    public VrstaIspita pronadjiPoId(Long id) {
        return vrstaIspitaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vrsta ispita nije pronađena."));
    }
}
