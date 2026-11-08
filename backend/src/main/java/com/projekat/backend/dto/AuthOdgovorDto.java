package com.projekat.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthOdgovorDto {
    private Long id;
    private String ime;
    private String prezime;
    private String username;
    private String email;
    private String tipNaloga;
    private String token;
}
