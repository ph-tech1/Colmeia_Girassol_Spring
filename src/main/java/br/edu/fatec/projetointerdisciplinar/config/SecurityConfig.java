package br.edu.fatec.projetointerdisciplinar.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/", "/index.html", "/Inicio", "/Sobre", "/Login",
                                "/login", "/html/login.html", "/html/cadastro.html",
                                "/api/pre-matriculas", "/css/**", "/js/**", "/images/**",
                                "/webjars/**", "/favicon.ico").permitAll()
                        .requestMatchers("/html/dashboard_responsavel.html").hasRole("RESPONSAVEL")
                        .requestMatchers("/html/dashboard_professor.html").hasRole("PROFESSOR")
                        .requestMatchers("/html/dashboard_admin.html").hasRole("ADMIN")
                        .requestMatchers("/api/**").hasAnyRole("PROFESSOR", "ADMIN")
                        .requestMatchers("/html/**").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/Login")
                        .loginProcessingUrl("/login")
                        .failureUrl("/html/login.html?error")
                        .successHandler((request, response, authentication) -> {
                            boolean professor = authentication.getAuthorities().stream()
                                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROFESSOR"));
                            boolean responsavel = authentication.getAuthorities().stream()
                                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_RESPONSAVEL"));
                            boolean admin = authentication.getAuthorities().stream()
                                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

                            if (admin) {
                                response.sendRedirect(request.getContextPath() + "/html/dashboard_admin.html");
                            } else if (professor) {
                                response.sendRedirect(request.getContextPath() + "/html/dashboard_professor.html");
                            } else if (responsavel) {
                                response.sendRedirect(request.getContextPath() + "/html/dashboard_responsavel.html");
                            } else {
                                response.sendRedirect(request.getContextPath() + "/html/login.html?error");
                            }
                        })
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/html/login.html?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());

        return http.build();
    }
}
