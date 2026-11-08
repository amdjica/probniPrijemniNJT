package com.projekat.backend.service;

import com.projekat.backend.dto.NacinPlacanja;
import com.projekat.backend.dto.PrijavaDto;
import com.projekat.backend.dto.PrijavaKreiranjeZahtevDto;
import com.projekat.backend.dto.PrijavaOdlukaOdgovorDto;
import com.projekat.backend.dto.PrijavaStatistikaDto;
import com.projekat.backend.dto.UplataDto;
import com.projekat.backend.entity.Administrator;
import com.projekat.backend.entity.Kandidat;
import com.projekat.backend.entity.Prijava;
import com.projekat.backend.entity.StatusPrijave;
import com.projekat.backend.entity.Termin;
import com.projekat.backend.entity.Uplata;
import com.projekat.backend.repository.PrijavaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PrijavaService {

    private static final double TOLERANCIJA = 0.01;
    private static final int BROJ_RATA = 5;

    /**
     * Prijava (po modelu) nema atribut za način plaćanja, pa se ta informacija kodira
     * kao prva linija postojećeg napomena polja. Ostatak napomene (npr. razlog odbijanja)
     * ide iza ove oznake i to je jedini deo koji se prikazuje korisniku kao "napomena".
     */
    private static final Pattern OZNAKA_NACINA_PLACANJA = Pattern.compile("^\\[NACIN_PLACANJA:(JEDNOKRATNO|RATE)]\\n?");

    private final PrijavaRepository prijavaRepository;
    private final TerminService terminService;
    private final KandidatService kandidatService;
    private final AdministratorService administratorService;
    private final UplataService uplataService;
    private final JavaMailSender mailSender;

    @Value("${app.payment.account-number}")
    private String brojRacuna;

    @Value("${app.payment.payment-purpose}")
    private String svrhaUplate;

    @Value("${app.payment.reference-prefix}")
    private String prefiksPozivaNaBroj;

    public PrijavaService(PrijavaRepository prijavaRepository,
                           TerminService terminService,
                           KandidatService kandidatService,
                           AdministratorService administratorService,
                           @Lazy UplataService uplataService,
                           JavaMailSender mailSender) {
        this.prijavaRepository = prijavaRepository;
        this.terminService = terminService;
        this.kandidatService = kandidatService;
        this.administratorService = administratorService;
        this.uplataService = uplataService;
        this.mailSender = mailSender;
    }

    @Transactional
    public PrijavaDto kreirajPrijavu(Long kandidatId, PrijavaKreiranjeZahtevDto zahtev) {
        Kandidat kandidat = kandidatService.pronadjiPoId(kandidatId);
        List<Termin> termini = terminService.pronadjiSvePoIdjevima(zahtev.terminiId());

        for (Termin termin : termini) {
            if (!Boolean.TRUE.equals(termin.getDostupan())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Termin " + termin.getDatum() + " trenutno nije otvoren za prijavljivanje.");
            }
            if (!termin.getDatum().isAfter(LocalDate.now())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Nije moguće prijaviti se na termin koji je već prošao ili je zakazan za danas.");
            }
            boolean vecPrijavljen = termin.getPrijave().stream()
                    .anyMatch(p -> p.getKandidat().getId().equals(kandidatId) && p.getStatus() != StatusPrijave.ODBIJENA);
            if (vecPrijavljen) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Već postoji aktivna prijava za termin " + termin.getDatum() + " u " + termin.getVremePocetka() + ".");
            }
        }

        double cena = termini.stream().mapToDouble(terminService::izracunajCenuTermina).sum();

        Prijava prijava = new Prijava();
        prijava.setDatumPrijave(LocalDate.now());
        prijava.setCena(cena);
        prijava.setStatus(StatusPrijave.NA_CEKANJU);
        prijava.setKandidat(kandidat);
        prijava.setTermini(new ArrayList<>(termini));
        prijava.setNapomena(kodirajNapomenu(zahtev.nacinPlacanja(), null));

        prijava = prijavaRepository.save(prijava);
        posaljiInstrukcijeZaUplatu(prijava);

        return mapDto(prijava);
    }

    public List<PrijavaDto> getMojePrijave(Long kandidatId) {
        return prijavaRepository.findByKandidatIdOrderByDatumPrijaveDesc(kandidatId).stream()
                .map(this::mapDto)
                .toList();
    }

    public PrijavaDto getDetalji(Long id, Long kandidatIdAkoJeKandidat) {
        Prijava prijava = pronadjiPoId(id);
        proveriVlasnistvo(prijava, kandidatIdAkoJeKandidat);
        return mapDto(prijava);
    }

    public List<PrijavaDto> pretraziPrijave(String ime, String email, Long vrstaIspitaId, LocalDate datumTermina, String status) {
        return prijavaRepository.findAll().stream()
                .filter(p -> ime == null || ime.isBlank()
                        || (p.getKandidat().getIme() + " " + p.getKandidat().getPrezime())
                        .toLowerCase().contains(ime.toLowerCase()))
                .filter(p -> email == null || email.isBlank()
                        || p.getKandidat().getEmail().toLowerCase().contains(email.toLowerCase()))
                .filter(p -> vrstaIspitaId == null
                        || p.getTermini().stream().anyMatch(t -> t.getVrstaIspita().getId().equals(vrstaIspitaId)))
                .filter(p -> datumTermina == null
                        || p.getTermini().stream().anyMatch(t -> t.getDatum().equals(datumTermina)))
                .filter(p -> status == null || status.isBlank() || p.getStatus().name().equalsIgnoreCase(status))
                .sorted(Comparator.comparing(Prijava::getDatumPrijave).reversed())
                .map(this::mapDto)
                .toList();
    }

    @Transactional
    public PrijavaOdlukaOdgovorDto odobriPrijavu(Long adminId, Long prijavaId) {
        Prijava prijava = pronadjiPoId(prijavaId);
        if (prijava.getStatus() == StatusPrijave.ODOBRENA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Prijava je već odobrena.");
        }

        double uplaceno = zbirUplata(prijava);
        if (Math.abs(uplaceno - prijava.getCena()) > TOLERANCIJA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Prijava još uvek ne može biti odobrena jer ukupan evidentirani iznos uplata (%.2f) nije jednak ceni prijave (%.2f).",
                            uplaceno, prijava.getCena()));
        }

        Administrator administrator = administratorService.pronadjiPoId(adminId);
        prijava.setStatus(StatusPrijave.ODOBRENA);
        prijava.setAdministrator(administrator);
        prijava = prijavaRepository.save(prijava);

        boolean poslato = posaljiObavestenjeOOdluci(prijava, true);
        return new PrijavaOdlukaOdgovorDto(mapDto(prijava), poslato);
    }

    @Transactional
    public PrijavaOdlukaOdgovorDto odbijPrijavu(Long adminId, Long prijavaId, String napomena) {
        Prijava prijava = pronadjiPoId(prijavaId);
        if (prijava.getStatus() == StatusPrijave.ODOBRENA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Odobrena prijava se ne može naknadno odbiti.");
        }

        prijava.setStatus(StatusPrijave.ODBIJENA);
        prijava.setNapomena(kodirajNapomenu(procitajNacinPlacanja(prijava.getNapomena()), napomena));
        prijava = prijavaRepository.save(prijava);

        boolean poslato = posaljiObavestenjeOOdluci(prijava, false);
        return new PrijavaOdlukaOdgovorDto(mapDto(prijava), poslato);
    }

    public PrijavaStatistikaDto getStatistika() {
        List<Prijava> sve = prijavaRepository.findAll();

        Map<String, Long> poStatusu = sve.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));

        Map<String, Long> poNacinuPlacanja = sve.stream()
                .collect(Collectors.groupingBy(p -> procitajNacinPlacanja(p.getNapomena()).name(), Collectors.counting()));

        Map<String, Double> ocekivaniPrihodPoVrstiIspita = sve.stream()
                .flatMap(p -> p.getTermini().stream())
                .collect(Collectors.groupingBy(t -> t.getVrstaIspita().getNaziv(),
                        Collectors.summingDouble(terminService::izracunajCenuTermina)));

        Map<String, Long> brojPrijavljenihPoLokaciji = sve.stream()
                .flatMap(p -> p.getTermini().stream())
                .collect(Collectors.groupingBy(t -> t.getLokacija().getNaziv(), Collectors.counting()));

        long saUplatnicom = sve.stream()
                .filter(p -> p.getUplate().stream().anyMatch(u -> uplataService.postojiUplatnica(u.getId())))
                .count();
        long bezUplatnice = sve.size() - saUplatnicom;

        double prosecanBrojTerminaPoKandidatu = sve.isEmpty() ? 0.0
                : sve.stream().mapToInt(p -> p.getTermini().size()).average().orElse(0.0);

        double ukupnoNaplaceno = sve.stream().mapToDouble(this::zbirUplata).sum();

        return new PrijavaStatistikaDto((long) sve.size(), poStatusu, poNacinuPlacanja, ocekivaniPrihodPoVrstiIspita,
                brojPrijavljenihPoLokaciji, saUplatnicom, bezUplatnice, prosecanBrojTerminaPoKandidatu, ukupnoNaplaceno);
    }

    public Prijava pronadjiPoId(Long id) {
        return prijavaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prijava nije pronađena."));
    }

    public void proveriVlasnistvo(Prijava prijava, Long kandidatIdAkoJeKandidat) {
        if (kandidatIdAkoJeKandidat != null && !prijava.getKandidat().getId().equals(kandidatIdAkoJeKandidat)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Prijava ne pripada prijavljenom kandidatu.");
        }
    }

    private double zbirUplata(Prijava prijava) {
        return prijava.getUplate().stream().mapToDouble(Uplata::getIznos).sum();
    }

    private boolean posaljiObavestenjeOOdluci(Prijava prijava, boolean odobrena) {
        try {
            SimpleMailMessage poruka = new SimpleMailMessage();
            poruka.setTo(prijava.getKandidat().getEmail());
            if (odobrena) {
                poruka.setSubject("Prijava odobrena - broj " + prijava.getId());
                StringBuilder telo = new StringBuilder();
                telo.append("Poštovani/a ").append(prijava.getKandidat().getIme()).append(",\n\n")
                        .append("Vaša prijava broj ").append(prijava.getId()).append(" je odobrena. ")
                        .append("Termini na koje ste prijavljeni:\n");
                for (Termin t : prijava.getTermini()) {
                    telo.append("- ").append(t.getVrstaIspita().getNaziv()).append(", ")
                            .append(t.getDatum()).append(" u ").append(t.getVremePocetka())
                            .append(", ").append(t.getLokacija().getNaziv())
                            .append(" (").append(t.getLokacija().getAdresa()).append(")\n");
                }
                telo.append("\nVidimo se na ispitu!\nOrganizacioni tim probnog prijemnog");
                poruka.setText(telo.toString());
            } else {
                poruka.setSubject("Prijava nije odobrena - broj " + prijava.getId());
                poruka.setText("Poštovani/a " + prijava.getKandidat().getIme() + ",\n\n"
                        + "Nažalost, Vaša prijava broj " + prijava.getId() + " nije odobrena.\n"
                        + "Napomena administratora: " + procitajVidljivuNapomenu(prijava.getNapomena()) + "\n\n"
                        + "Za dodatne informacije obratite nam se putem kontakt email adrese sa sajta.\n\n"
                        + "Organizacioni tim probnog prijemnog");
            }
            mailSender.send(poruka);
            return true;
        } catch (MailException ex) {
            return false;
        }
    }

    private void posaljiInstrukcijeZaUplatu(Prijava prijava) {
        try {
            NacinPlacanja nacin = procitajNacinPlacanja(prijava.getNapomena());
            SimpleMailMessage poruka = new SimpleMailMessage();
            poruka.setTo(prijava.getKandidat().getEmail());
            poruka.setSubject("Podaci za uplatu probnog prijemnog ispita");

            String imePrezime = prijava.getKandidat().getIme() + " " + prijava.getKandidat().getPrezime();
            String pozivNaBroj = prefiksPozivaNaBroj + "-" + prijava.getId() + "-" + prijava.getKandidat().getId();

            StringBuilder telo = new StringBuilder();
            telo.append("Poštovani/a ").append(imePrezime).append(",\n\n")
                    .append("Vaša prijava je evidentirana u sistemu pod brojem ").append(prijava.getId()).append(".\n\n")
                    .append("Izabrani termini:\n");
            for (Termin t : prijava.getTermini()) {
                telo.append("- ").append(t.getVrstaIspita().getNaziv()).append(", ")
                        .append(t.getDatum()).append(" u ").append(t.getVremePocetka())
                        .append(", ").append(t.getLokacija().getNaziv())
                        .append(" (").append(t.getLokacija().getAdresa()).append(")\n");
            }
            telo.append("\nUkupan iznos za uplatu:\n").append(String.format("%.2f RSD", prijava.getCena())).append("\n\n")
                    .append("Način plaćanja:\n");
            if (nacin == NacinPlacanja.RATE) {
                double iznosRate = izracunajIznosRate(prijava.getCena());
                telo.append("Plaćanje u ").append(BROJ_RATA).append(" rata\n")
                        .append("Iznos jedne rate: ").append(String.format("%.2f RSD", iznosRate)).append("\n")
                        .append("Broj rata: ").append(BROJ_RATA).append("\n\n");
            } else {
                telo.append("Jednokratno\n\n");
            }
            telo.append("Podaci za popunjavanje uplatnice:\n")
                    .append("Broj računa: ").append(brojRacuna).append("\n")
                    .append("Poziv na broj: ").append(pozivNaBroj).append("\n")
                    .append("Svrha uplate: ").append(svrhaUplate).append(" - ").append(imePrezime).append("\n\n")
                    .append("Nakon uplate, u aplikaciji priložite sliku ili dokument uplatnice u okviru svoje prijave. ")
                    .append("Prijava će biti odobrena tek kada zbir evidentiranih uplata bude jednak ukupnoj ceni prijave.\n\n")
                    .append("Srdačno,\nTim za organizaciju probnog prijemnog");

            poruka.setText(telo.toString());
            mailSender.send(poruka);
        } catch (MailException ex) {
            // Neuspelo slanje emaila ne sme da poništi već evidentiranu prijavu.
        }
    }

    private String kodirajNapomenu(NacinPlacanja nacin, String vidljivaNapomena) {
        String oznaka = "[NACIN_PLACANJA:" + nacin.name() + "]";
        if (vidljivaNapomena == null || vidljivaNapomena.isBlank()) {
            return oznaka;
        }
        return oznaka + "\n" + vidljivaNapomena;
    }

    private NacinPlacanja procitajNacinPlacanja(String napomena) {
        if (napomena == null) {
            return NacinPlacanja.JEDNOKRATNO;
        }
        Matcher m = OZNAKA_NACINA_PLACANJA.matcher(napomena);
        return m.find() ? NacinPlacanja.valueOf(m.group(1)) : NacinPlacanja.JEDNOKRATNO;
    }

    private String procitajVidljivuNapomenu(String napomena) {
        if (napomena == null) {
            return null;
        }
        String ostatak = OZNAKA_NACINA_PLACANJA.matcher(napomena).replaceFirst("");
        return ostatak.isBlank() ? null : ostatak;
    }

    private double izracunajIznosRate(double cena) {
        return cena / BROJ_RATA;
    }

    PrijavaDto mapDto(Prijava p) {
        double uplaceno = zbirUplata(p);
        double preostalo = Math.max(0.0, p.getCena() - uplaceno);
        boolean mozeDaDodaUplatu = p.getStatus() != StatusPrijave.ODOBRENA;
        boolean mozeDaSeOdobri = p.getStatus() != StatusPrijave.ODOBRENA && Math.abs(uplaceno - p.getCena()) <= TOLERANCIJA;
        boolean mozeDaSeOdbije = p.getStatus() != StatusPrijave.ODOBRENA;
        NacinPlacanja nacin = procitajNacinPlacanja(p.getNapomena());

        PrijavaDto dto = new PrijavaDto();
        dto.setId(p.getId());
        dto.setDatumPrijave(p.getDatumPrijave());
        dto.setStatus(p.getStatus().name());
        dto.setCena(p.getCena());
        dto.setUplaceno(uplaceno);
        dto.setPreostalo(preostalo);
        dto.setNacinPlacanja(nacin.name());
        if (nacin == NacinPlacanja.RATE) {
            dto.setBrojRata(BROJ_RATA);
            dto.setIznosJedneRate(izracunajIznosRate(p.getCena()));
        }
        dto.setNapomena(procitajVidljivuNapomenu(p.getNapomena()));
        dto.setMozeDaDodaUplatu(mozeDaDodaUplatu);
        dto.setMozeDaSeOdobri(mozeDaSeOdobri);
        dto.setMozeDaSeOdbije(mozeDaSeOdbije);
        dto.setKandidatId(p.getKandidat().getId());
        dto.setKandidatIme(p.getKandidat().getIme());
        dto.setKandidatPrezime(p.getKandidat().getPrezime());
        dto.setKandidatEmail(p.getKandidat().getEmail());
        dto.setTermini(p.getTermini().stream().map(terminService::mapDto).toList());
        dto.setUplate(p.getUplate().stream()
                .sorted(Comparator.comparing(Uplata::getDatumUplate))
                .map(u -> new UplataDto(u.getId(), u.getIznos(), u.getDatumUplate(),
                        uplataService.postojiUplatnica(u.getId())))
                .toList());
        return dto;
    }
}
