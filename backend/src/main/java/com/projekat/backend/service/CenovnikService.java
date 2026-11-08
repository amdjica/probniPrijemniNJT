package com.projekat.backend.service;

import com.projekat.backend.dto.CenovnikDto;
import com.projekat.backend.dto.CenovnikZahtevDto;
import com.projekat.backend.entity.Cenovnik;
import com.projekat.backend.entity.VrstaIspitaCenovnik;
import com.projekat.backend.repository.CenovnikRepository;
import com.projekat.backend.repository.VrstaIspitaCenovnikRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CenovnikService {

    private final CenovnikRepository cenovnikRepository;
    private final VrstaIspitaCenovnikRepository vrstaIspitaCenovnikRepository;

    public CenovnikService(CenovnikRepository cenovnikRepository,
                            VrstaIspitaCenovnikRepository vrstaIspitaCenovnikRepository) {
        this.cenovnikRepository = cenovnikRepository;
        this.vrstaIspitaCenovnikRepository = vrstaIspitaCenovnikRepository;
    }

    public List<CenovnikDto> getAktivneCenovnike() {
        return vrstaIspitaCenovnikRepository.findByAktivnaTrue().stream()
                .map(this::mapDto)
                .toList();
    }

    @Transactional
    public CenovnikDto azurirajCenu(Long vrstaIspitaId, CenovnikZahtevDto zahtev) {
        VrstaIspitaCenovnik veza = vrstaIspitaCenovnikRepository.findByVrstaIspitaIdAndAktivnaTrue(vrstaIspitaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Za izabranu vrstu ispita ne postoji aktivan cenovnik."));

        Cenovnik cenovnik = veza.getCenovnik();
        cenovnik.setNaziv(zahtev.naziv());
        cenovnik.setCena(zahtev.cena());
        cenovnik.setVaziOd(zahtev.vaziOd());
        cenovnik.setVaziDo(zahtev.vaziDo());
        cenovnik.setOpis(zahtev.opis());
        cenovnikRepository.save(cenovnik);

        return mapDto(veza);
    }

    private CenovnikDto mapDto(VrstaIspitaCenovnik veza) {
        Cenovnik c = veza.getCenovnik();
        return new CenovnikDto(c.getId(), c.getNaziv(), c.getCena(), c.getVaziOd(), c.getVaziDo(), c.getOpis(),
                veza.getVrstaIspita().getId(), veza.getVrstaIspita().getNaziv(), veza.getAktivna());
    }
}
