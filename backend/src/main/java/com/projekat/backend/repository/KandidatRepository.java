package com.projekat.backend.repository;

import com.projekat.backend.entity.Kandidat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KandidatRepository extends JpaRepository<Kandidat, Long> {
    Optional<Kandidat> findByEmail(String email);
    boolean existsByEmail(String email);
}
