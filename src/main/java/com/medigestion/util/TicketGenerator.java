package com.medigestion.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Generador de códigos de ticket únicos para reservas.
 * Formato: MG-XXXXX-YY donde X=alfanumérico, Y=secuencial
 */
public class TicketGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("ddMM");

    /**
     * Genera un código de ticket único con formato MG-XXXX-MMDD
     */
    public static String generarCodigo() {
        StringBuilder sb = new StringBuilder("MG-");
        for (int i = 0; i < 4; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        sb.append("-");
        sb.append(LocalDateTime.now().format(FMT));
        return sb.toString();
    }

    /**
     * Determina el turno basado en la hora.
     */
    public static String determinarTurno(LocalDateTime hora) {
        int h = hora.getHour();
        if (h < 12) return "Manana";
        if (h < 18) return "Tarde";
        return "Noche";
    }
}
