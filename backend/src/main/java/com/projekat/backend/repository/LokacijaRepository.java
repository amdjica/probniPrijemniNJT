package com.projekat.backend.repository;

import com.projekat.backend.entity.Lokacija;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LokacijaRepository extends JpaRepository<Lokacija, Long> {
}
