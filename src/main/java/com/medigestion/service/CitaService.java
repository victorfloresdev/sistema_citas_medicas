package com.medigestion.service;

import com.medigestion.dao.*;
import com.medigestion.model.*;
import com.medigestion.util.TicketGenerator;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

/**
 * Servicio central para la gestión y reserva de citas médicas.
 * Implementa RF-02, RF-03, RF-04, RF-05, RF-07, RF-08, RF-10.
 */
public class CitaService {

    private final OfertaMedicaDAO ofertaMedicaDAO = new OfertaMedicaDAO();
    private final ReservaCitaDAO reservaCitaDAO = new ReservaCitaDAO();
    private final EspecialidadDAO especialidadDAO = new EspecialidadDAO();
    private final DemandaNoAtendidaDAO demandaDAO = new DemandaNoAtendidaDAO();
    private final BitacoraEventoDAO bitacoraDAO = new BitacoraEventoDAO();
    private final NotificacionService notificacionService = new NotificacionService();

    /**
     * Calcula el porcentaje de afluencia/congestión del día actual (RF-03).
     * Basado en la densidad de citas activas del día respecto a la capacidad operativa.
     */
    public int calcularAfluenciaActual() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX);

        List<ReservaCita> citasHoy = reservaCitaDAO.findCitasActivasDelDia(inicioDia, finDia);
        // Capacidad estimada estándar por día en el establecimiento (ejemplo: 50 citas diarias)
        int capacidadInstalada = 50;
        int citasCount = citasHoy.size();

        int porcentaje = (int) Math.min(100, Math.round(((double) citasCount / capacidadInstalada) * 100));
        return Math.max(porcentaje, 15); // Nivel base de actividad
    }

    /**
     * Obtiene la lista de especialidades médicas activas.
     */
    public List<Especialidad> listarEspecialidades() {
        return especialidadDAO.findAll();
    }

    /**
     * Busca las ofertas de citas disponibles para una especialidad dada (RF-04).
     * Si no hay cupos, registra la demanda no atendida (RF-07).
     */
    public List<OfertaMedica> buscarOfertasDisponibles(Integer idEspecialidad, Paciente paciente) {
        List<OfertaMedica> ofertas = ofertaMedicaDAO.findDisponiblesByEspecialidad(idEspecialidad);

        if (ofertas.isEmpty() && paciente != null) {
            // RF-07: Demanda No Atendida
            Especialidad esp = especialidadDAO.findById(idEspecialidad).orElse(null);
            if (esp != null) {
                DemandaNoAtendida dna = new DemandaNoAtendida();
                dna.setEspecialidad(esp);
                dna.setPaciente(paciente);
                demandaDAO.save(dna);

                bitacoraDAO.save(new BitacoraEvento("CitaService", "WARN",
                        "Demanda no atendida registrada para paciente " + paciente.getDni() + 
                        " en especialidad: " + esp.getNombreEspecialidad()));
            }
        }

        return ofertas;
    }

    /**
     * Realiza la reserva de una cita médica (RF-04, RF-05, RF-08).
     * Aplica la regla de negocio de no permitir 2 citas en la misma especialidad en la misma semana.
     */
    public ReservaCita reservarCita(Paciente paciente, Integer idOferta) {
        OfertaMedica oferta = ofertaMedicaDAO.findByIdConDetalles(idOferta)
                .orElseThrow(() -> new IllegalArgumentException("La oferta médica no existe."));

        if (!"Disponible".equalsIgnoreCase(oferta.getEstado())) {
            throw new IllegalStateException("El horario seleccionado ya no está disponible. Por favor elija otro.");
        }

        // Regla de Negocio RF-04: Verificar que no tenga ya una cita activa en la misma especialidad en la misma semana
        LocalDateTime fechaCita = oferta.getHorario().getFechaHoraInicio();
        LocalDateTime inicioSemana = fechaCita.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
        LocalDateTime finSemana = fechaCita.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate().atTime(LocalTime.MAX);

        long citasMismaSemana = reservaCitaDAO.countCitasActivasSemanaPorEspecialidad(
                paciente.getDni(),
                oferta.getEspecialidad().getIdEspecialidad(),
                inicioSemana,
                finSemana
        );

        if (citasMismaSemana > 0) {
            throw new IllegalStateException(
                    "Regla de negocio: El paciente ya cuenta con una cita activa para la especialidad " +
                    oferta.getEspecialidad().getNombreEspecialidad() + " en la misma semana.");
        }

        // Marcar la oferta como Reservada
        oferta.setEstado("Reservado");
        ofertaMedicaDAO.update(oferta);

        // Crear la reserva de cita (RF-05)
        String ticket = TicketGenerator.generarCodigo();
        String turno = TicketGenerator.determinarTurno(fechaCita);

        ReservaCita reserva = new ReservaCita();
        reserva.setPaciente(paciente);
        reserva.setOfertaMedica(oferta);
        reserva.setCodigoTicket(ticket);
        reserva.setEstado("Confirmada");
        reserva.setTurno(turno);

        ReservaCita guardada = reservaCitaDAO.save(reserva);

        // Enviar notificación automática (RF-08)
        notificacionService.enviarNotificacionConfirmacion(guardada);

        bitacoraDAO.save(new BitacoraEvento("CitaService", "INFO",
                "Cita confirmada con Ticket " + ticket + " para paciente " + paciente.getDni()));

        return guardada;
    }

    /**
     * Cancela una reserva de cita y libera el cupo inmediatamente (RF-10, RF-11).
     * Si el canal es WEB, valida la restricción de 1 hora de anticipación mínima.
     */
    public void cancelarCita(Integer idReserva, String canal, String motivo) {
        ReservaCita reserva = reservaCitaDAO.findByIdConDetalles(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada."));

        if ("Cancelada".equalsIgnoreCase(reserva.getEstado())) {
            throw new IllegalStateException("La cita ya se encuentra cancelada.");
        }

        LocalDateTime fechaHoraCita = reserva.getOfertaMedica().getHorario().getFechaHoraInicio();
        LocalDateTime ahora = LocalDateTime.now();

        // RF-10: Restricción de 1 hora para canal WEB
        if ("WEB".equalsIgnoreCase(canal)) {
            Duration duracion = Duration.between(ahora, fechaHoraCita);
            if (duracion.toMinutes() < 60) {
                throw new IllegalStateException(
                        "Cancelación denegada vía WEB: Faltan menos de 60 minutos para la cita. " +
                        "Debe realizar la cancelación justificada de manera presencial en ventanilla.");
            }
        }

        // Actualizar reserva
        reserva.setEstado("Cancelada");
        reserva.setFechaCancelacion(LocalDateTime.now());
        reserva.setCanalCancelacion(canal);
        reserva.setMotivoCancelacion(motivo);
        reservaCitaDAO.update(reserva);

        // Liberar el cupo inmediatamente (RF-11)
        OfertaMedica oferta = reserva.getOfertaMedica();
        oferta.setEstado("Disponible");
        ofertaMedicaDAO.update(oferta);

        // Despachar notificación de cancelación
        notificacionService.enviarNotificacionCancelacion(reserva, motivo);

        bitacoraDAO.save(new BitacoraEvento("CitaService", "INFO",
                "Cita " + reserva.getCodigoTicket() + " cancelada vía " + canal + ". Cupo liberado."));
    }

    /**
     * Lista las citas de un paciente.
     */
    public List<ReservaCita> listarCitasPorPaciente(String dni) {
        return reservaCitaDAO.findByPacienteDni(dni);
    }

    /**
     * Busca una reserva por código de ticket.
     */
    public Optional<ReservaCita> buscarPorTicket(String codigo) {
        return reservaCitaDAO.findByCodigoTicket(codigo);
    }
}
