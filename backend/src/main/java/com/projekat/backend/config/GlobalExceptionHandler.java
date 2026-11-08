package com.projekat.backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> obradiStatusniIzuzetak(ResponseStatusException ex) {
        String poruka = ex.getReason() != null ? ex.getReason() : "Došlo je do greške.";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("poruka", poruka));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> obradiValidacionuGresku(MethodArgumentNotValidException ex) {
        String poruka = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(m -> m != null && !m.isBlank())
                .collect(Collectors.joining(" "));
        if (poruka.isBlank()) {
            poruka = "Uneti podaci nisu validni.";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("poruka", poruka));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> obradiNedostajuciParametar(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("poruka", "Nedostaje obavezan podatak: " + ex.getParameterName() + "."));
    }
}
