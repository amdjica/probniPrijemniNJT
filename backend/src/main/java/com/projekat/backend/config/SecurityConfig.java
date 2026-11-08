package com.projekat.backend.config;

import com.projekat.backend.security.JwtAccessDeniedHandler;
import com.projekat.backend.security.JwtAuthEntryPoint;
import com.projekat.backend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                           JwtAuthEntryPoint jwtAuthEntryPoint,
                           JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/nalozi/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/vrste-ispita/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lokacije/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/termini").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/termini/*/prijavljeni-kandidati").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/api/termini").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/termini/*").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/termini/*").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/cenovnici").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/cenovnici/*").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/api/prijave").hasRole("KANDIDAT")
                        .requestMatchers(HttpMethod.GET, "/api/prijave/moje").hasRole("KANDIDAT")
                        .requestMatchers(HttpMethod.GET, "/api/prijave/statistika").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/prijave").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/prijave/*/odobri").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/prijave/*/odbij").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.POST, "/api/prijave/*/uplate").hasRole("KANDIDAT")
                        .requestMatchers(HttpMethod.GET, "/api/prijave/*/uplate/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/termini/*/izvestaj").hasRole("ADMINISTRATOR")
                        .requestMatchers(HttpMethod.GET, "/api/prijave/*").authenticated()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
