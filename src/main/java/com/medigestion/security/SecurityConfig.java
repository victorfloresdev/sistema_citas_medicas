package com.medigestion.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración central de seguridad Spring Security 6 (Hito 70% - Pruebas de Seguridad).
 * Aplica:
 * - RBAC (Control de acceso basado en roles)
 * - Protección CSRF en formularios Thymeleaf
 * - BCrypt con factor de costo 12
 * - Cabeceras de seguridad HTTP (XSS, FrameOptions)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", 
                        "/login", 
                        "/registro", 
                        "/sala-espera/**", 
                        "/css/**", 
                        "/js/**", 
                        "/images/**", 
                        "/webjars/**", 
                        "/h2-console/**", 
                        "/actuator/health", 
                        "/actuator/info"
                ).permitAll()
                .requestMatchers("/paciente/**").hasRole("PACIENTE")
                .requestMatchers("/medico/**").hasRole("MEDICO")
                .requestMatchers("/admin/**", "/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/default", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }
}
