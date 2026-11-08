package com.projekat.backend.service;

import com.projekat.backend.dto.LokacijaDto;
import com.projekat.backend.repository.LokacijaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LokacijaService {

    private final LokacijaRepository lokacijaRepository;

    public LokacijaService(LokacijaRepository lokacijaRepository) {
        this.lokacijaRepository = lokacijaRepository;
    }

    public List<LokacijaDto> getSve() {
        return lokacijaRepository.findAll().stream()
                .map(l -> new LokacijaDto(l.getId(), l.getNaziv(), l.getAdresa()))
                .toList();
    }
}
