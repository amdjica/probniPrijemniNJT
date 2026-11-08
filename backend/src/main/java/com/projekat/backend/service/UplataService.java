package com.projekat.backend.service;

import com.projekat.backend.dto.UplataDto;
import com.projekat.backend.entity.Prijava;
import com.projekat.backend.entity.StatusPrijave;
import com.projekat.backend.entity.Uplata;
import com.projekat.backend.repository.UplataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class UplataService {

    private static final double TOLERANCIJA = 0.01;

    private final UplataRepository uplataRepository;
    private final PrijavaService prijavaService;

    @Value("${app.upload.dir}")
    private String direktorijumUplata;

    public UplataService(UplataRepository uplataRepository, PrijavaService prijavaService) {
        this.uplataRepository = uplataRepository;
        this.prijavaService = prijavaService;
    }

    @Transactional
    public UplataDto dodajUplatu(Long kandidatId, Long prijavaId, Double iznos, LocalDate datumUplate, MultipartFile uplatnica) {
        Prijava prijava = prijavaService.pronadjiPoId(prijavaId);
        prijavaService.proveriVlasnistvo(prijava, kandidatId);

        if (prijava.getStatus() == StatusPrijave.ODOBRENA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Prijava je već odobrena, dodatna uplata nije potrebna.");
        }

        if (uplatnica == null || uplatnica.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Priložite sliku ili dokument uplatnice uz evidentiranu uplatu.");
        }

        double vecUplaceno = prijava.getUplate().stream().mapToDouble(Uplata::getIznos).sum();
        double preostalo = prijava.getCena() - vecUplaceno;

        if (iznos > preostalo + TOLERANCIJA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Iznos uplate (%.2f) premašuje preostali iznos za uplatu (%.2f).",
                            iznos, Math.max(0.0, preostalo)));
        }

        Uplata uplata = new Uplata();
        uplata.setIznos(iznos);
        uplata.setDatumUplate(datumUplate != null ? datumUplate : LocalDate.now());
        uplata.setPrijava(prijava);
        uplata = uplataRepository.save(uplata);

        sacuvajUplatnicu(uplata.getId(), uplatnica);

        return new UplataDto(uplata.getId(), uplata.getIznos(), uplata.getDatumUplate(), true);
    }

    public List<UplataDto> getUplateZaPrijavu(Long prijavaId, Long kandidatIdAkoJeKandidat) {
        Prijava prijava = prijavaService.pronadjiPoId(prijavaId);
        prijavaService.proveriVlasnistvo(prijava, kandidatIdAkoJeKandidat);

        return uplataRepository.findByPrijavaId(prijavaId).stream()
                .sorted(Comparator.comparing(Uplata::getDatumUplate))
                .map(u -> new UplataDto(u.getId(), u.getIznos(), u.getDatumUplate(), postojiUplatnica(u.getId())))
                .toList();
    }

    public Resource getUplatnica(Long prijavaId, Long uplataId, Long kandidatIdAkoJeKandidat) {
        Prijava prijava = prijavaService.pronadjiPoId(prijavaId);
        prijavaService.proveriVlasnistvo(prijava, kandidatIdAkoJeKandidat);

        Uplata uplata = uplataRepository.findById(uplataId)
                .filter(u -> u.getPrijava().getId().equals(prijavaId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uplata nije pronađena."));

        return pronadjiUplatnicu(uplata.getId())
                .map(FileSystemResource::new)
                .map(Resource.class::cast)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uplatnica nije priložena za ovu uplatu."));
    }

    /**
     * Da li je za datu uplatu priložena uplatnica. Koristi i PrijavaService (statistika), zato je javna.
     */
    public boolean postojiUplatnica(Long uplataId) {
        return pronadjiUplatnicu(uplataId).isPresent();
    }

    /**
     * Uplata (po modelu) nema atribut za putanju priložene uplatnice, pa se dokument ne evidentira
     * ni u entitetu ni u bazi - čuva se isključivo na disku, po konvenciji imena "uplata-{id}.<ekstenzija>",
     * a pronalazi se listanjem direktorijuma po prefiksu.
     */
    private void sacuvajUplatnicu(Long uplataId, MultipartFile fajl) {
        try {
            Path koren = Path.of(direktorijumUplata);
            Files.createDirectories(koren);

            obrisiPostojecuUplatnicu(uplataId);

            String originalno = fajl.getOriginalFilename() != null ? fajl.getOriginalFilename() : "uplatnica";
            String ekstenzija = originalno.contains(".") ? originalno.substring(originalno.lastIndexOf('.')) : "";
            Path putanja = koren.resolve("uplata-" + uplataId + ekstenzija);
            fajl.transferTo(putanja);
        } catch (IOException ex) {
            throw new UncheckedIOException("Uplatnica nije mogla biti sačuvana.", ex);
        }
    }

    private Optional<Path> pronadjiUplatnicu(Long uplataId) {
        Path koren = Path.of(direktorijumUplata);
        if (!Files.isDirectory(koren)) {
            return Optional.empty();
        }
        String prefiks = "uplata-" + uplataId + ".";
        try (var stream = Files.list(koren)) {
            return stream
                    .filter(p -> p.getFileName().toString().startsWith(prefiks))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .findFirst();
        } catch (IOException ex) {
            throw new UncheckedIOException("Greška pri čitanju direktorijuma sa uplatnicama.", ex);
        }
    }

    private void obrisiPostojecuUplatnicu(Long uplataId) throws IOException {
        Optional<Path> postojeca = pronadjiUplatnicu(uplataId);
        if (postojeca.isPresent()) {
            Files.deleteIfExists(postojeca.get());
        }
    }
}
