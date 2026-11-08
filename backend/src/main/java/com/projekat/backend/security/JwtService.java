package com.projekat.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKey kljuc;
    private final long trajanjeMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.trajanje-ms}") long trajanjeMs) {
        this.kljuc = Keys.hmacShaKeyFor(secret.getBytes());
        this.trajanjeMs = trajanjeMs;
    }

    public String generisiToken(Long id, String username, String uloga) {
        Date sada = new Date();
        Date istice = new Date(sada.getTime() + trajanjeMs);
        return Jwts.builder()
                .subject(username)
                .claim("id", String.valueOf(id))
                .claim("uloga", uloga)
                .issuedAt(sada)
                .expiration(istice)
                .signWith(kljuc)
                .compact();
    }

    public Claims raspakujToken(String token) {
        return Jwts.parser()
                .verifyWith(kljuc)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
