package com.medigestion;

import com.medigestion.util.DataSeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Clase principal de inicio de la aplicación web Spring Boot 3 - MediGestión.
 * Hito 80%: Despliegue en AWS y soporte multi-rol (Paciente, Médico, Administrador).
 */
@SpringBootApplication
public class MediGestionApplication {

    private static final Logger log = LoggerFactory.getLogger(MediGestionApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MediGestionApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("==================================================================");
            log.info("  MEDIGESTIÓN 80% - SISTEMA INTEGRAL DE CITAS MÉDICAS");
            log.info("  Despliegue: Spring Boot 3 + Thymeleaf + AWS Ready");
            log.info("==================================================================");
            try {
                DataSeeder.seed();
                log.info("  ✔ Datos maestros inicializados correctamente.");
            } catch (Exception e) {
                log.warn("  ⚠ Inicialización de datos maestros advertencia: {}", e.getMessage());
            }
        };
    }
}
