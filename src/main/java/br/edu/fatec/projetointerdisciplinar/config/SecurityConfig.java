package br.edu.fatec.projetointerdisciplinar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        return http
                .authorizeHttpRequests(auth -> auth
                        // Páginas públicas
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/Inicio",
                                "/Login",
                                "/LoginResponsavel",

                                // Arquivos do frontend
                                "/html/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",

                                // Endpoint que futuramente fará o login
                                "/api/login"
                        ).permitAll()

                        // Todo o restante precisa estar autenticado
                        .anyRequest().authenticated()
                )

                // Como estamos trabalhando com uma API REST,
                // vamos deixar CSRF desativado neste primeiro momento.
                .csrf(csrf -> csrf.disable())

                .build();
    }
}