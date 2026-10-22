package com.medigestion.service;

import com.medigestion.dao.*;
import com.medigestion.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión administrativa y panel de control (BPMN-4, RF-11, RF-12, RF-14).
 */
public class AdminService {

    private final AdministradorDAO adminDAO = new AdministradorDAO();
    private final OfertaMedicaDAO ofertaMedicaDAO = new OfertaMedicaDAO();
    private final ReservaCitaDAO reservaCitaDAO = new ReservaCitaDAO();
    private final EspecialidadDAO especialidadDAO = new EspecialidadDAO();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final BitacoraEventoDAO bitacoraDAO = new BitacoraEventoDAO();

    public Optional<Administrador> loginAdmin(String email) {
        return adminDAO.findByEmail(email);
    }

    public List<OfertaMedica> listarTodasLasOfertas() {
        return ofertaMedicaDAO.findAllConDetalles();
    }

    public List<Medico> listarMedicos() {
        return medicoDAO.findActivos();
    }

    /**
     * Devuelve métricas consolidadas para el Dashboard gerencial (RF-14).
     */
    public DashboardMetricas obtenerMetricasDashboard() {
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy = LocalDate.now().atTime(23, 59, 59);

        List<ReservaCita> citasHoy = reservaCitaDAO.findCitasActivasDelDia(inicioHoy, finHoy);
        List<OfertaMedica> todasOfertas = ofertaMedicaDAO.findAll();
        List<Medico> medicos = medicoDAO.findAll();
        List<Especialidad> especialidades = especialidadDAO.findAll();

        long citasCompletadas = todasOfertas.stream().filter(o -> "Reservado".equals(o.getEstado())).count();

        return new DashboardMetricas(
                citasHoy.size(),
                medicos.size(),
                especialidades.size(),
                todasOfertas.size(),
                citasCompletadas
        );
    }

    public static class DashboardMetricas {
        public final int citasHoy;
        public final int totalMedicos;
        public final int totalEspecialidades;
        public final int totalOfertasProgramadas;
        public final long citasReservadas;

        public DashboardMetricas(int citasHoy, int totalMedicos, int totalEspecialidades, 
                                 int totalOfertasProgramadas, long citasReservadas) {
            this.citasHoy = citasHoy;
            this.totalMedicos = totalMedicos;
            this.totalEspecialidades = totalEspecialidades;
            this.totalOfertasProgramadas = totalOfertasProgramadas;
            this.citasReservadas = citasReservadas;
        }
    }
}
