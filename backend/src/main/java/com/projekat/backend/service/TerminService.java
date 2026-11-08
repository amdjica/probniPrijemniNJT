package com.projekat.backend.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.projekat.backend.dto.KandidatZaTerminDto;
import com.projekat.backend.dto.TerminDto;
import com.projekat.backend.dto.TerminZahtevDto;
import com.projekat.backend.entity.Lokacija;
import com.projekat.backend.entity.Prijava;
import com.projekat.backend.entity.StatusPrijave;
import com.projekat.backend.entity.Termin;
import com.projekat.backend.entity.VrstaIspita;
import com.projekat.backend.entity.VrstaIspitaCenovnik;
import com.projekat.backend.repository.LokacijaRepository;
import com.projekat.backend.repository.TerminRepository;
import com.projekat.backend.repository.VrstaIspitaCenovnikRepository;
import com.projekat.backend.repository.VrstaIspitaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class TerminService {

    private final TerminRepository terminRepository;
    private final VrstaIspitaRepository vrstaIspitaRepository;
    private final LokacijaRepository lokacijaRepository;
    private final VrstaIspitaCenovnikRepository vrstaIspitaCenovnikRepository;

    public TerminService(TerminRepository terminRepository,
                          VrstaIspitaRepository vrstaIspitaRepository,
                          LokacijaRepository lokacijaRepository,
                          VrstaIspitaCenovnikRepository vrstaIspitaCenovnikRepository) {
        this.terminRepository = terminRepository;
        this.vrstaIspitaRepository = vrstaIspitaRepository;
        this.lokacijaRepository = lokacijaRepository;
        this.vrstaIspitaCenovnikRepository = vrstaIspitaCenovnikRepository;
    }

    public List<TerminDto> getSvi(Long vrstaIspitaId, Long prijavljenKandidatId) {
        List<Termin> termini = vrstaIspitaId != null
                ? terminRepository.findByVrstaIspitaId(vrstaIspitaId)
                : terminRepository.findAll();

        return termini.stream()
                .filter(t -> Boolean.TRUE.equals(t.getDostupan()))
                .filter(t -> !jeKandidatVecPrijavljen(t, prijavljenKandidatId))
                .map(this::mapDto)
                .toList();
    }

    @Transactional
    public TerminDto kreiraj(TerminZahtevDto zahtev) {
        VrstaIspita vrstaIspita = vrstaIspitaRepository.findById(zahtev.vrstaIspitaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Izabrana vrsta ispita ne postoji."));
        Lokacija lokacija = lokacijaRepository.findById(zahtev.lokacijaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Izabrana lokacija ne postoji."));

        Termin termin = new Termin();
        termin.setDatum(zahtev.datum());
        termin.setVremePocetka(zahtev.vremePocetka());
        termin.setNapomena(zahtev.napomena());
        termin.setDostupan(zahtev.dostupan() != null ? zahtev.dostupan() : Boolean.TRUE);
        termin.setVrstaIspita(vrstaIspita);
        termin.setLokacija(lokacija);

        return mapDto(terminRepository.save(termin));
    }

    @Transactional
    public TerminDto azuriraj(Long id, TerminZahtevDto zahtev) {
        Termin termin = pronadjiPoId(id);
        proveriDaLiSeMozeMenjatiIliObrisati(termin);

        VrstaIspita vrstaIspita = vrstaIspitaRepository.findById(zahtev.vrstaIspitaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Izabrana vrsta ispita ne postoji."));
        Lokacija lokacija = lokacijaRepository.findById(zahtev.lokacijaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Izabrana lokacija ne postoji."));

        termin.setDatum(zahtev.datum());
        termin.setVremePocetka(zahtev.vremePocetka());
        termin.setNapomena(zahtev.napomena());
        termin.setDostupan(zahtev.dostupan() != null ? zahtev.dostupan() : termin.getDostupan());
        termin.setVrstaIspita(vrstaIspita);
        termin.setLokacija(lokacija);

        return mapDto(terminRepository.save(termin));
    }

    @Transactional
    public void obrisi(Long id) {
        Termin termin = pronadjiPoId(id);
        proveriDaLiSeMozeMenjatiIliObrisati(termin);
        terminRepository.delete(termin);
    }

    public List<KandidatZaTerminDto> getPrijavljeneKandidate(Long terminId) {
        Termin termin = pronadjiPoId(terminId);
        return termin.getPrijave().stream()
                .filter(p -> p.getStatus() == StatusPrijave.ODOBRENA)
                .map(p -> new KandidatZaTerminDto(p.getKandidat().getIme(), p.getKandidat().getPrezime(),
                        p.getKandidat().getEmail(), p.getStatus().name()))
                .toList();
    }

    public byte[] generisiIzvestajKandidata(Long terminId) {
        Termin termin = pronadjiPoId(terminId);
        List<Prijava> prijavljeni = termin.getPrijave().stream()
                .filter(p -> p.getStatus() == StatusPrijave.ODOBRENA)
                .sorted(Comparator.comparing(p -> p.getKandidat().getPrezime()))
                .toList();

        try (ByteArrayOutputStream izlaz = new ByteArrayOutputStream()) {
            Document dokument = new Document(PageSize.A4, 40, 40, 50, 50);
            PdfWriter.getInstance(dokument, izlaz);
            dokument.open();

            Font naslovFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font podnaslovFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font zaglavljeFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font redFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            dokument.add(new Paragraph("Spisak prijavljenih kandidata", naslovFont));
            dokument.add(new Paragraph(termin.getVrstaIspita().getNaziv(), podnaslovFont));
            dokument.add(new Paragraph(
                    termin.getDatum() + " u " + termin.getVremePocetka() + " - " + termin.getLokacija().getNaziv(),
                    podnaslovFont));
            Paragraph razmak = new Paragraph(" ");
            dokument.add(razmak);

            PdfPTable tabela = new PdfPTable(new float[]{1, 3, 3, 4});
            tabela.setWidthPercentage(100);

            for (String naslov : List.of("Br.", "Ime", "Prezime", "Email")) {
                PdfPCell celija = new PdfPCell(new Paragraph(naslov, zaglavljeFont));
                celija.setHorizontalAlignment(Element.ALIGN_LEFT);
                celija.setPadding(6);
                tabela.addCell(celija);
            }

            int redniBroj = 1;
            for (Prijava p : prijavljeni) {
                tabela.addCell(new PdfPCell(new Paragraph(String.valueOf(redniBroj++), redFont)));
                tabela.addCell(new PdfPCell(new Paragraph(p.getKandidat().getIme(), redFont)));
                tabela.addCell(new PdfPCell(new Paragraph(p.getKandidat().getPrezime(), redFont)));
                tabela.addCell(new PdfPCell(new Paragraph(p.getKandidat().getEmail(), redFont)));
            }

            if (prijavljeni.isEmpty()) {
                PdfPCell prazno = new PdfPCell(new Paragraph("Za ovaj termin trenutno nema odobrenih prijava.", redFont));
                prazno.setColspan(4);
                prazno.setPadding(8);
                tabela.addCell(prazno);
            }

            dokument.add(tabela);
            dokument.close();

            return izlaz.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Izveštaj nije mogao biti generisan.", ex);
        } catch (java.io.IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Termin pronadjiPoId(Long id) {
        return terminRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Termin nije pronađen."));
    }

    public List<Termin> pronadjiSvePoIdjevima(List<Long> idjevi) {
        List<Termin> termini = terminRepository.findAllById(idjevi);
        if (termini.size() != idjevi.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Jedan ili više izabranih termina ne postoji.");
        }
        return termini;
    }

    public Double izracunajCenuTermina(Termin termin) {
        VrstaIspitaCenovnik veza = vrstaIspitaCenovnikRepository
                .findByVrstaIspitaIdAndAktivnaTrue(termin.getVrstaIspita().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Za vrstu ispita '" + termin.getVrstaIspita().getNaziv() + "' trenutno ne postoji aktivan cenovnik."));
        return veza.getCenovnik().getCena();
    }

    private boolean jeKandidatVecPrijavljen(Termin termin, Long kandidatId) {
        if (kandidatId == null) {
            return false;
        }
        return termin.getPrijave().stream()
                .anyMatch(p -> p.getKandidat().getId().equals(kandidatId) && p.getStatus() != StatusPrijave.ODBIJENA);
    }

    private void proveriDaLiSeMozeMenjatiIliObrisati(Termin termin) {
        if (!termin.getPrijave().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Termin se ne može menjati niti obrisati jer već ima prijavljenih kandidata.");
        }
        if (!termin.getDatum().isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Termin koji je već prošao ili je zakazan za danas se ne može menjati niti obrisati.");
        }
    }

    TerminDto mapDto(Termin t) {
        Double cena;
        try {
            cena = izracunajCenuTermina(t);
        } catch (ResponseStatusException ex) {
            cena = null;
        }
        boolean mozeDaSeMenja = t.getPrijave().isEmpty() && t.getDatum().isAfter(LocalDate.now());
        return new TerminDto(t.getId(), t.getDatum(), t.getVremePocetka(), t.getNapomena(), t.getDostupan(),
                t.getVrstaIspita().getId(), t.getVrstaIspita().getNaziv(),
                t.getLokacija().getId(), t.getLokacija().getNaziv(), t.getLokacija().getAdresa(),
                cena, t.getPrijave().size(), mozeDaSeMenja, mozeDaSeMenja);
    }
}
