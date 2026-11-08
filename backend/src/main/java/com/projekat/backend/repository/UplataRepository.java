package com.projekat.backend.repository;

import com.projekat.backend.entity.Uplata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UplataRepository extends JpaRepository<Uplata, Long> {
    List<Uplata> findByPrijavaId(Long prijavaId);
}
