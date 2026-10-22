package com.medigestion.service;

import com.medigestion.dao.ReservaCitaDAO;
import com.medigestion.dao.BitacoraEventoDAO;
import com.medigestion.model.ReservaCita;
import com.medigestion.model.BitacoraEvento;

import java.time.LocalDateTime;

/**
 * Servicio para la atención en consultorio y sala de espera (BPMN-3, RF-13).
 */
public class AtencionService {

    private final ReservaCitaDAO reservaCitaDAO = new ReservaCitaDAO();
    private final BitacoraEventoDAO bitacoraDAO = new BitacoraEventoDAO();

    /**
     * El médico presiona el botón de llamada para actualizar la pantalla de sala de espera.
     */
    public ReservaCita llamarPaciente(Integer idReserva) {
        ReservaCita r = reservaCitaDAO.findByIdConDetalles(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada."));

        r.setFechaLlamado(LocalDateTime.now());
        r.setFechaPresentacion(LocalDateTime.now());
        ReservaCita actualizada = reservaCitaDAO.update(r);

        bitacoraDAO.save(new BitacoraEvento("AtencionService", "INFO",
                "Paciente " + r.getPaciente().getNombreCompleto() + " llamado al consultorio " + 
                r.getOfertaMedica().getConsultorio()));

        return actualizada;
    }

    /**
     * El médico atiende al paciente y finaliza la cita.
     */
    public ReservaCita marcarCompletada(Integer idReserva) {
        ReservaCita r = reservaCitaDAO.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada."));

        r.setEstado("Completada");
        ReservaCita actualizada = reservaCitaDAO.update(r);

        bitacoraDAO.save(new BitacoraEvento("AtencionService", "INFO",
                "Cita completada exitosamente: " + r.getCodigoTicket()));

        return actualizada;
    }

    /**
     * Si el paciente no asiste al consultorio, el médico lo marca como ausente.
     */
    public ReservaCita marcarAusente(Integer idReserva) {
        ReservaCita r = reservaCitaDAO.findById(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada."));

        r.setEstado("Ausente");
        ReservaCita actualizada = reservaCitaDAO.update(r);

        bitacoraDAO.save(new BitacoraEvento("AtencionService", "WARN",
                "Paciente no asistió. Marcado como Ausente en ticket: " + r.getCodigoTicket()));

        return actualizada;
    }
}
