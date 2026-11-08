package com.projekat.backend.repository;

import com.projekat.backend.entity.VrstaIspitaCenovnik;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VrstaIspitaCenovnikRepository extends JpaRepository<VrstaIspitaCenovnik, Long> {
    Optional<VrstaIspitaCenovnik> findByVrstaIspitaIdAndAktivnaTrue(Long vrstaIspitaId);
    List<VrstaIspitaCenovnik> findByAktivnaTrue();
}
