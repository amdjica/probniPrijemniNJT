package com.projekat.backend.repository;

import com.projekat.backend.entity.Prijava;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrijavaRepository extends JpaRepository<Prijava, Long> {
    List<Prijava> findByKandidatIdOrderByDatumPrijaveDesc(Long kandidatId);
}
