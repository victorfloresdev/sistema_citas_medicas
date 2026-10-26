package com.medigestion.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.medigestion.dao.*;
import com.medigestion.model.*;
import com.medigestion.util.TicketGenerator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Aplica principios SOLID (SRP, DIP, LSP, ISP, OCP) y recursos Java (Google Guava, Apache Commons, Logback).
 * Implementa RF-02, RF-03, RF-04, RF-05, RF-07, RF-08, RF-10 en trazabilidad con BPM-1 y BPM-2.
 */
public class CitaService {

    private static final Logger log = LoggerFactory.getLogger(CitaService.class);

    private final IOfertaMedicaDAO ofertaMedicaDAO;
    private final IReservaCitaDAO reservaCitaDAO;
    private final IEspecialidadDAO especialidadDAO;
    private final IDemandaNoAtendidaDAO demandaDAO;
    private final IBitacoraEventoDAO bitacoraDAO;
    private final NotificacionService notificacionService;

    /**
     * Constructor por defecto: inicializa dependencias reales para consola (JPA/Hibernate).
     */
    public CitaService() {
        this(new OfertaMedicaDAO(), new ReservaCitaDAO(), new EspecialidadDAO(),
             new DemandaNoAtendidaDAO(), new BitacoraEventoDAO(), new NotificacionService());
    }

    /**
     * Constructor con inyección de dependencias para Principio de Inversión de Dependencias (DIP) y TDD.
     */
    public CitaService(IOfertaMedicaDAO ofertaMedicaDAO,
                       IReservaCitaDAO reservaCitaDAO,
                       IEspecialidadDAO especialidadDAO,
                       IDemandaNoAtendidaDAO demandaDAO,
                       IBitacoraEventoDAO bitacoraDAO,
                       NotificacionService notificacionService) {
        this.ofertaMedicaDAO = Preconditions.checkNotNull(ofertaMedicaDAO, "ofertaMedicaDAO no puede ser nulo");
        this.reservaCitaDAO = Preconditions.checkNotNull(reservaCitaDAO, "reservaCitaDAO no puede ser nulo");
        this.especialidadDAO = Preconditions.checkNotNull(especialidadDAO, "especialidadDAO no puede ser nulo");
        this.demandaDAO = Preconditions.checkNotNull(demandaDAO, "demandaDAO no puede ser nulo");
        this.bitacoraDAO = Preconditions.checkNotNull(bitacoraDAO, "bitacoraDAO no puede ser nulo");
        this.notificacionService = Preconditions.checkNotNull(notificacionService, "notificacionService no puede ser nulo");
    }

    /**
     * Calcula el porcentaje de afluencia/congestión del día actual (RF-02, RF-03).
     * Basado en la densidad de citas activas del día respecto a la capacidad operativa.
     */
    public int calcularAfluenciaActual() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(LocalTime.MAX);

        List<ReservaCita> citasHoy = reservaCitaDAO.findCitasActivasDelDia(inicioDia, finDia);
        int capacidadInstalada = 50;
        int citasCount = citasHoy.size();

        int porcentaje = (int) Math.min(100, Math.round(((double) citasCount / capacidadInstalada) * 100));
        int resultado = Math.max(porcentaje, 15);
        log.info("[RF-03 Afluencia] Calculado: {}% con {} citas activas hoy (Capacidad: {})", resultado, citasCount, capacidadInstalada);
        return resultado;
    }

    /**
     * Obtiene la lista inmutable de especialidades médicas activas usando Google Guava.
     */
    public List<Especialidad> listarEspecialidades() {
        return ImmutableList.copyOf(especialidadDAO.findAll());
    }

    /**
     * Busca las ofertas de citas disponibles para una especialidad dada (RF-04).
     * Si no hay cupos, registra la demanda no atendida (RF-07).
     */
    public List<OfertaMedica> buscarOfertasDisponibles(Integer idEspecialidad, Paciente paciente) {
        Preconditions.checkNotNull(idEspecialidad, "El ID de la especialidad es obligatorio.");
        List<OfertaMedica> ofertas = ofertaMedicaDAO.findDisponiblesByEspecialidad(idEspecialidad);

        if (ofertas.isEmpty() && paciente != null) {
            // RF-07: Registro de Demanda No Atendida
            Especialidad esp = especialidadDAO.findById(idEspecialidad).orElse(null);
            if (esp != null) {
                DemandaNoAtendida dna = new DemandaNoAtendida();
                dna.setEspecialidad(esp);
                dna.setPaciente(paciente);
                demandaDAO.save(dna);

                String msg = String.format("Demanda no atendida registrada para paciente %s en especialidad: %s",
                        paciente.getDni(), esp.getNombreEspecialidad());
                log.warn("[RF-07 Demanda No Atendida] {}", msg);
                bitacoraDAO.save(new BitacoraEvento("CitaService", "WARN", msg));
            }
        } else {
            log.info("[RF-04 Ofertas] Se encontraron {} cupos disponibles para especialidad ID {}", ofertas.size(), idEspecialidad);
        }

        return ImmutableList.copyOf(ofertas);
    }

    /**
     * Realiza la reserva de una cita médica (RF-04, RF-05, RF-08).
     * Aplica la regla de negocio crítica: no permitir más de 1 cita activa en la misma especialidad en la misma semana.
     * Implementa validaciones robustas mediante Google Guava Preconditions y Apache Commons.
     */
    public ReservaCita reservarCita(Paciente paciente, Integer idOferta) {
        Preconditions.checkNotNull(paciente, "El paciente no puede ser nulo.");
        Preconditions.checkNotNull(idOferta, "El ID de la oferta no puede ser nulo.");

        // Camino 1 Caja Blanca: Oferta inexistente
        OfertaMedica oferta = ofertaMedicaDAO.findByIdConDetalles(idOferta)
                .orElseThrow(() -> new IllegalArgumentException("La oferta médica no existe."));

        // Camino 2 Caja Blanca: Oferta no disponible
        Preconditions.checkState("Disponible".equalsIgnoreCase(oferta.getEstado()),
                "El horario seleccionado ya no está disponible. Por favor elija otro.");

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

        // Camino 3 Caja Blanca: Violación de regla de negocio de 1 cita semanal por especialidad
        if (citasMismaSemana > 0) {
            String warnMsg = String.format("Regla de negocio: El paciente %s ya cuenta con una cita activa para la especialidad %s en la misma semana.",
                    paciente.getDni(), oferta.getEspecialidad().getNombreEspecialidad());
            log.warn("[RF-04 Bloqueo Cita Duplicada] {}", warnMsg);
            throw new IllegalStateException(warnMsg);
        }

        // Camino 4 Caja Blanca: Reserva confirmada exitosamente
        oferta.setEstado("Reservado");
        ofertaMedicaDAO.update(oferta);

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

        String logMsg = String.format("Cita confirmada con Ticket %s para paciente %s en consultorio %s",
                ticket, paciente.getDni(), oferta.getMedico().getConsultorio());
        log.info("[RF-05 Ticket Emitido] {}", logMsg);
        bitacoraDAO.save(new BitacoraEvento("CitaService", "INFO", logMsg));

        return guardada;
    }

    /**
     * Cancela una reserva de cita y libera el cupo inmediatamente (RF-06, RF-10, RF-11).
     * Si el canal es WEB, valida la restricción de 60 minutos de anticipación mínima.
     */
    public void cancelarCita(Integer idReserva, String canal, String motivo) {
        Preconditions.checkNotNull(idReserva, "El ID de la reserva no puede ser nulo.");
        String canalNormalizado = StringUtils.defaultIfBlank(canal, "WEB").toUpperCase();

        // Camino 1 Caja Blanca: Reserva inexistente
        ReservaCita reserva = reservaCitaDAO.findByIdConDetalles(idReserva)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada."));

        // Camino 2 Caja Blanca: Reserva ya cancelada previamente
        Preconditions.checkState(!"Cancelada".equalsIgnoreCase(reserva.getEstado()),
                "La cita ya se encuentra cancelada.");

        LocalDateTime fechaHoraCita = reserva.getOfertaMedica().getHorario().getFechaHoraInicio();
        LocalDateTime ahora = LocalDateTime.now();

        // Camino 3 Caja Blanca: Cancelación denegada por ventana de 60 minutos en canal WEB (RF-06)
        if ("WEB".equalsIgnoreCase(canalNormalizado)) {
            Duration duracion = Duration.between(ahora, fechaHoraCita);
            if (duracion.toMinutes() < 60) {
                String errorMsg = "Cancelación denegada vía WEB: Faltan menos de 60 minutos para la cita. " +
                        "Debe realizar la cancelación justificada de manera presencial en ventanilla.";
                log.warn("[RF-06 Regla 60 Min] Cita {}: {}", reserva.getCodigoTicket(), errorMsg);
                throw new IllegalStateException(errorMsg);
            }
        }

        // Camino 4 Caja Blanca: Cancelación exitosa y liberación inmediata de cupo
        reserva.setEstado("Cancelada");
        reserva.setFechaCancelacion(LocalDateTime.now());
        reserva.setCanalCancelacion(canalNormalizado);
        reserva.setMotivoCancelacion(StringUtils.defaultIfBlank(motivo, "Cancelación solicitada por el asegurado"));
        reservaCitaDAO.update(reserva);

        // Liberar el cupo inmediatamente (RF-06, RF-11)
        OfertaMedica oferta = reserva.getOfertaMedica();
        oferta.setEstado("Disponible");
        ofertaMedicaDAO.update(oferta);

        notificacionService.enviarNotificacionCancelacion(reserva, motivo);

        String infoMsg = String.format("Cita %s cancelada vía %s. Cupo liberado inmediatamente a oferta general.",
                reserva.getCodigoTicket(), canalNormalizado);
        log.info("[RF-06 Cancelación Exitosa] {}", infoMsg);
        bitacoraDAO.save(new BitacoraEvento("CitaService", "INFO", infoMsg));
    }

    /**
     * Lista inmutable de citas de un paciente usando Guava.
     */
    public List<ReservaCita> listarCitasPorPaciente(String dni) {
        Preconditions.checkArgument(StringUtils.isNotBlank(dni), "El DNI no puede estar vacío.");
        return ImmutableList.copyOf(reservaCitaDAO.findByPacienteDni(dni));
    }

    /**
     * Busca una reserva por código de ticket.
     */
    public Optional<ReservaCita> buscarPorTicket(String codigo) {
        if (StringUtils.isBlank(codigo)) {
            return Optional.empty();
        }
        return reservaCitaDAO.findByCodigoTicket(codigo.trim().toUpperCase());
    }

    /**
     * Lista todas las citas activas o históricas para reportería oficial.
     */
    public List<ReservaCita> listarTodasLasCitas() {
        return ImmutableList.copyOf(reservaCitaDAO.findAll());
    }
}
