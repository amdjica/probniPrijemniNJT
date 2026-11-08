package com.projekat.backend.repository;

import com.projekat.backend.entity.Termin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TerminRepository extends JpaRepository<Termin, Long> {
    List<Termin> findByVrstaIspitaId(Long vrstaIspitaId);
}
