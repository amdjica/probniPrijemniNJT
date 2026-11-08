package com.projekat.backend.dto;

import java.time.LocalDate;

public record UplataDto(Long id, Double iznos, LocalDate datumUplate, Boolean imaUplatnicu) {
}
