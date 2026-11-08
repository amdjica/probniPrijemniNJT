package com.projekat.backend.repository;

import com.projekat.backend.entity.Cenovnik;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CenovnikRepository extends JpaRepository<Cenovnik, Long> {
}
