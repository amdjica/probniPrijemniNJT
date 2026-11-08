package com.projekat.backend.service;

import com.projekat.backend.dto.AuthOdgovorDto;
import com.projekat.backend.dto.LoginZahtevDto;
import com.projekat.backend.entity.Administrator;
import com.projekat.backend.repository.AdministratorRepository;
import com.projekat.backend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdministratorService {

    private final AdministratorRepository administratorRepository;
    private final JwtService jwtService;

    public AdministratorService(AdministratorRepository administratorRepository,
                                 JwtService jwtService) {
        this.administratorRepository = administratorRepository;
        this.jwtService = jwtService;
    }

    public AuthOdgovorDto prijaviSe(LoginZahtevDto zahtev) {
        Administrator administrator = administratorRepository.findByEmail(zahtev.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Uneta lozinka nije ispravna. Proverite podatke i pokušajte ponovo."));

        if (!zahtev.password().equals(administrator.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Uneta lozinka nije ispravna. Proverite podatke i pokušajte ponovo.");
        }

        String token = jwtService.generisiToken(administrator.getId(), administrator.getUsername(), "ADMINISTRATOR");
        return new AuthOdgovorDto(administrator.getId(), administrator.getIme(), administrator.getPrezime(),
                administrator.getUsername(), administrator.getEmail(), "ADMINISTRATOR", token);
    }

    public Administrator pronadjiPoId(Long id) {
        return administratorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nalog administratora nije pronađen."));
    }
}
