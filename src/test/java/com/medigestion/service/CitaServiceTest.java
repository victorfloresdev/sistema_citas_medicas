package com.medigestion.service;

import com.medigestion.dao.*;
import com.medigestion.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias y funcionales de Caja Blanca (TDD) para CitaService.
 * Aplica aislamiento estricto mediante Mockito sobre interfaces DAO (DIP, LSP).
 * Cobertura de caminos independientes y complejidad ciclomática de McCabe.
 */
@ExtendWith(MockitoExtension.class)
public class CitaServiceTest {

    @Mock
    private IOfertaMedicaDAO ofertaMedicaDAO;

    @Mock
    private IReservaCitaDAO reservaCitaDAO;

    @Mock
    private IEspecialidadDAO especialidadDAO;

    @Mock
    private IDemandaNoAtendidaDAO demandaDAO;

    @Mock
    private IBitacoraEventoDAO bitacoraDAO;

    @Mock
    private NotificacionService notificacionService;

    private CitaService citaService;
    private Paciente pacienteDemo;
    private Especialidad especialidadDemo;
    private Medico medicoDemo;
    private Horario horarioDemo;
    private OfertaMedica ofertaDemo;

    @BeforeEach
    void setUp() {
        citaService = new CitaService(
                ofertaMedicaDAO,
                reservaCitaDAO,
                especialidadDAO,
                demandaDAO,
                bitacoraDAO,
                notificacionService
        );

        pacienteDemo = new Paciente();
        pacienteDemo.setDni("45892134");
        pacienteDemo.setNombres("Carlos");
        pacienteDemo.setApellidos("Mendoza Rivera");
        pacienteDemo.setEmail("carlos.mendoza@email.com");

        especialidadDemo = new Especialidad();
        especialidadDemo.setIdEspecialidad(1);
        especialidadDemo.setNombreEspecialidad("Cardiología");

        medicoDemo = new Medico();
        medicoDemo.setIdMedico(101);
        medicoDemo.setNombres("Sarah");
        medicoDemo.setApellidos("Jenkins");
        medicoDemo.setConsultorio("C-101");

        horarioDemo = new Horario();
        horarioDemo.setIdHorario(201);
        // Fecha futura: dentro de 3 días a las 10:00 AM
        horarioDemo.setFechaHoraInicio(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0));
        horarioDemo.setFechaHoraFin(LocalDateTime.now().plusDays(3).withHour(10).withMinute(30));

        ofertaDemo = new OfertaMedica();
        ofertaDemo.setIdOferta(500);
        ofertaDemo.setEspecialidad(especialidadDemo);
        ofertaDemo.setMedico(medicoDemo);
        ofertaDemo.setHorario(horarioDemo);
        ofertaDemo.setEstado("Disponible");
    }

    // =========================================================================
    // PRUEBAS DE CAJA BLANCA: reservarCita()
    // Grafo de Flujo de Control con 4 caminos independientes (Complejidad McCabe = 4)
    // =========================================================================

    @Test
    @DisplayName("Caja Blanca 1 - Camino 1: Rechazar reserva cuando la oferta médica no existe")
    void testReservarCita_Camino1_OfertaInexistente() {
        when(ofertaMedicaDAO.findByIdConDetalles(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citaService.reservarCita(pacienteDemo, 999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La oferta médica no existe");

        verify(ofertaMedicaDAO, never()).update(any());
        verify(reservaCitaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Caja Blanca 1 - Camino 2: Rechazar reserva cuando el horario ya está ocupado/reservado")
    void testReservarCita_Camino2_HorarioYaOcupado() {
        ofertaDemo.setEstado("Reservado");
        when(ofertaMedicaDAO.findByIdConDetalles(500)).thenReturn(Optional.of(ofertaDemo));

        assertThatThrownBy(() -> citaService.reservarCita(pacienteDemo, 500))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya no está disponible");

        verify(reservaCitaDAO, never()).save(any());
        verify(notificacionService, never()).enviarNotificacionConfirmacion(any());
    }

    @Test
    @DisplayName("Caja Blanca 1 - Camino 3: Rechazar reserva por regla de negocio de 1 cita semanal por especialidad (RF-04)")
    void testReservarCita_Camino3_ReglaUnaCitaSemanalViolada() {
        when(ofertaMedicaDAO.findByIdConDetalles(500)).thenReturn(Optional.of(ofertaDemo));
        // Simular que el paciente ya tiene 1 cita en la misma semana
        when(reservaCitaDAO.countCitasActivasSemanaPorEspecialidad(eq("45892134"), eq(1), any(), any()))
                .thenReturn(1L);

        assertThatThrownBy(() -> citaService.reservarCita(pacienteDemo, 500))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya cuenta con una cita activa para la especialidad Cardiología en la misma semana");

        verify(ofertaMedicaDAO, never()).update(any());
        verify(reservaCitaDAO, never()).save(any());
    }

    @Test
    @DisplayName("Caja Blanca 1 - Camino 4: Confirmación exitosa con código de ticket único y actualización de estado")
    void testReservarCita_Camino4_ReservaExitosa() {
        when(ofertaMedicaDAO.findByIdConDetalles(500)).thenReturn(Optional.of(ofertaDemo));
        when(reservaCitaDAO.countCitasActivasSemanaPorEspecialidad(eq("45892134"), eq(1), any(), any()))
                .thenReturn(0L);

        when(reservaCitaDAO.save(any(ReservaCita.class))).thenAnswer(invocation -> {
            ReservaCita r = invocation.getArgument(0);
            r.setIdReserva(777);
            return r;
        });

        ReservaCita resultado = citaService.reservarCita(pacienteDemo, 500);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstado()).isEqualTo("Confirmada");
        assertThat(resultado.getCodigoTicket()).startsWith("MG-");
        assertThat(resultado.getTurno()).isIn("Manana", "Mañana", "Tarde");
        assertThat(ofertaDemo.getEstado()).isEqualTo("Reservado");

        verify(ofertaMedicaDAO).update(ofertaDemo);
        verify(reservaCitaDAO).save(any(ReservaCita.class));
        verify(notificacionService).enviarNotificacionConfirmacion(any(ReservaCita.class));
        verify(bitacoraDAO).save(any(BitacoraEvento.class));
    }

    // =========================================================================
    // PRUEBAS DE CAJA BLANCA: cancelarCita()
    // Grafo de Flujo de Control con 4 caminos independientes (Complejidad McCabe = 4)
    // =========================================================================

    @Test
    @DisplayName("Caja Blanca 2 - Camino 1: Rechazar cancelación cuando la reserva no existe")
    void testCancelarCita_Camino1_ReservaInexistente() {
        when(reservaCitaDAO.findByIdConDetalles(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> citaService.cancelarCita(999, "WEB", "Motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Reserva no encontrada");

        verify(reservaCitaDAO, never()).update(any());
        verify(ofertaMedicaDAO, never()).update(any());
    }

    @Test
    @DisplayName("Caja Blanca 2 - Camino 2: Rechazar cancelación cuando la cita ya estaba cancelada")
    void testCancelarCita_Camino2_CitaYaCancelada() {
        ReservaCita citaCancelada = new ReservaCita();
        citaCancelada.setIdReserva(1);
        citaCancelada.setEstado("Cancelada");

        when(reservaCitaDAO.findByIdConDetalles(1)).thenReturn(Optional.of(citaCancelada));

        assertThatThrownBy(() -> citaService.cancelarCita(1, "WEB", "Motivo"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("La cita ya se encuentra cancelada");

        verify(reservaCitaDAO, never()).update(any());
    }

    @Test
    @DisplayName("Caja Blanca 2 - Camino 3: Rechazar cancelación vía WEB a menos de 60 minutos de la cita (RF-06)")
    void testCancelarCita_Camino3_CancelacionWebMenosDe60Minutos() {
        ReservaCita citaProxima = new ReservaCita();
        citaProxima.setIdReserva(2);
        citaProxima.setEstado("Confirmada");
        citaProxima.setCodigoTicket("MG-TEST-01");

        Horario horarioProximo = new Horario();
        // Cita programada dentro de solo 30 minutos
        horarioProximo.setFechaHoraInicio(LocalDateTime.now().plusMinutes(30));
        OfertaMedica om = new OfertaMedica();
        om.setHorario(horarioProximo);
        citaProxima.setOfertaMedica(om);

        when(reservaCitaDAO.findByIdConDetalles(2)).thenReturn(Optional.of(citaProxima));

        assertThatThrownBy(() -> citaService.cancelarCita(2, "WEB", "Urgencia"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Faltan menos de 60 minutos");

        verify(reservaCitaDAO, never()).update(any());
        verify(ofertaMedicaDAO, never()).update(any());
    }

    @Test
    @DisplayName("Caja Blanca 2 - Camino 4: Cancelación exitosa y liberación inmediata de cupo (RF-06, RF-11)")
    void testCancelarCita_Camino4_CancelacionExitosa() {
        ReservaCita citaValida = new ReservaCita();
        citaValida.setIdReserva(3);
        citaValida.setEstado("Confirmada");
        citaValida.setCodigoTicket("MG-OKAY-99");

        Horario horarioFuturo = new Horario();
        horarioFuturo.setFechaHoraInicio(LocalDateTime.now().plusHours(24));
        OfertaMedica om = new OfertaMedica();
        om.setHorario(horarioFuturo);
        om.setEstado("Reservado");
        citaValida.setOfertaMedica(om);

        when(reservaCitaDAO.findByIdConDetalles(3)).thenReturn(Optional.of(citaValida));

        citaService.cancelarCita(3, "WEB", "Viaje de trabajo");

        assertThat(citaValida.getEstado()).isEqualTo("Cancelada");
        assertThat(citaValida.getCanalCancelacion()).isEqualTo("WEB");
        assertThat(citaValida.getMotivoCancelacion()).isEqualTo("Viaje de trabajo");
        assertThat(om.getEstado()).isEqualTo("Disponible");

        verify(reservaCitaDAO).update(citaValida);
        verify(ofertaMedicaDAO).update(om);
        verify(notificacionService).enviarNotificacionCancelacion(eq(citaValida), anyString());
        verify(bitacoraDAO).save(any(BitacoraEvento.class));
    }

    // =========================================================================
    // PRUEBAS DE DEMANDA NO ATENDIDA Y AFLUENCIA (RF-03, RF-07)
    // =========================================================================

    @Test
    @DisplayName("RF-07 Demanda No Atendida: Registrar en bitácora y tabla cuando una especialidad no tiene cupos")
    void testBuscarOfertasDisponibles_RegistraDemandaNoAtendida() {
        when(ofertaMedicaDAO.findDisponiblesByEspecialidad(99)).thenReturn(Collections.emptyList());
        Especialidad espPediatria = new Especialidad();
        espPediatria.setIdEspecialidad(99);
        espPediatria.setNombreEspecialidad("Pediatría");
        when(especialidadDAO.findById(99)).thenReturn(Optional.of(espPediatria));

        List<OfertaMedica> resultado = citaService.buscarOfertasDisponibles(99, pacienteDemo);

        assertThat(resultado).isEmpty();
        verify(demandaDAO).save(any(DemandaNoAtendida.class));
        verify(bitacoraDAO).save(any(BitacoraEvento.class));
    }

    @Test
    @DisplayName("RF-03 Afluencia: Calcular indicador basado en la densidad de citas del día")
    void testCalcularAfluenciaActual() {
        when(reservaCitaDAO.findCitasActivasDelDia(any(), any()))
                .thenReturn(List.of(new ReservaCita(), new ReservaCita(), new ReservaCita(), new ReservaCita(), new ReservaCita()));

        int afluencia = citaService.calcularAfluenciaActual();

        // 5 citas / 50 = 10%, pero el nivel base es 15%
        assertThat(afluencia).isGreaterThanOrEqualTo(15);
    }
}
