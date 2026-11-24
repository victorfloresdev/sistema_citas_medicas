package com.medigestion.actuator;

import com.medigestion.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * HealthIndicator personalizado de Spring Boot Actuator para monitoreo operativo hospitalario (Hito 80%).
 * Evalúa el nivel de afluencia y alerta en caso de sobreocupación.
 */
@Component
public class CapacidadHospitalariaHealthIndicator implements HealthIndicator {

    private final CitaService citaService;

    @Autowired
    public CapacidadHospitalariaHealthIndicator(CitaService citaService) {
        this.citaService = citaService;
    }

    @Override
    public Health health() {
        try {
            int afluencia = citaService.calcularAfluenciaActual();

            if (afluencia >= 90) {
                return Health.down()
                        .withDetail("afluencia", afluencia + "%")
                        .withDetail("estado", "ALERTA_SATURACION_CRITICA")
                        .withDetail("mensaje", "La red hospitalaria se encuentra al límite de capacidad operativa.")
                        .build();
            } else if (afluencia >= 75) {
                return Health.status("WARN")
                        .withDetail("afluencia", afluencia + "%")
                        .withDetail("estado", "ALTA_DEMANDA")
                        .withDetail("mensaje", "Capacidad cercana al umbral preventivo.")
                        .build();
            }

            return Health.up()
                    .withDetail("afluencia", afluencia + "%")
                    .withDetail("estado", "OPERATIVO_NORMAL")
                    .withDetail("capacidadTotalDia", 50)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
