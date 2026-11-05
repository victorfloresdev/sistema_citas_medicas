package com.medigestion.service;

import com.medigestion.dao.IBitacoraEventoDAO;
import com.medigestion.dao.IDemandaNoAtendidaDAO;
import com.medigestion.dao.IEspecialidadDAO;
import com.medigestion.dao.IOfertaMedicaDAO;
import com.medigestion.dao.IReservaCitaDAO;
import com.medigestion.model.BitacoraEvento;
import com.medigestion.model.Especialidad;
import com.medigestion.model.Horario;
import com.medigestion.model.Medico;
import com.medigestion.model.OfertaMedica;
import com.medigestion.model.Paciente;
import com.medigestion.model.ReservaCita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ============================================================================
 * PRÁCTICA: PRUEBA Y EVIDENCIA SOBRE PROYECTO INTEGRADOR
 * CASO DE PRUEBA PRIORITARIO AUTOMATIZADO: CP-MED-01
 * ----------------------------------------------------------------------------
 * Requisito Asociado : RF-04 (Restricción de 1 Cita Semanal por Especialidad)
 * Flujo de Proceso   : BPM-1 (Solicitud y Reserva de Citas Médicas)
 * Nivel de Riesgo    : ALTO (Riesgo Crítico de Acaparamiento y Desabastecimiento)
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CP-MED-01: Validación Automatizada del Flujo Crítico de Regla de Negocio RF-04")
public class ReservaCitaPrioritariaAutomatedTest {

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

    // Fixtures de prueba representativos
    private Paciente pacienteAsegurado;
    private Especialidad especialidadCardiologia;
    private Especialidad especialidadOdontologia;
    private Medico medicoCardiologo;
    private OfertaMedica ofertaViernes;
    private OfertaMedica ofertaSemanaSiguiente;

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

        // Paciente de prueba
        pacienteAsegurado = new Paciente();
        pacienteAsegurado.setDni("45892134");
        pacienteAsegurado.setNombres("Carlos");
        pacienteAsegurado.setApellidos("Mendoza Rivera");
        pacienteAsegurado.setTelefono("987654321");
        pacienteAsegurado.setEmail("carlos.mendoza@gmail.com");

        // Especialidades
        especialidadCardiologia = new Especialidad();
        especialidadCardiologia.setIdEspecialidad(1);
        especialidadCardiologia.setNombreEspecialidad("Cardiología");

        especialidadOdontologia = new Especialidad();
        especialidadOdontologia.setIdEspecialidad(2);
        especialidadOdontologia.setNombreEspecialidad("Odontología");

        // Médico
        medicoCardiologo = new Medico();
        medicoCardiologo.setIdMedico(10);
        medicoCardiologo.setNombres("Dr. Roberto");
        medicoCardiologo.setApellidos("Gómez");
        medicoCardiologo.setConsultorio("C-101");

        // Fechas calculadas para asegurar que caigan dentro de la misma semana calendario
        LocalDate proximoViernes = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        Horario horarioViernes = new Horario();
        horarioViernes.setIdHorario(201);
        horarioViernes.setFechaHoraInicio(proximoViernes.atTime(10, 0));
        horarioViernes.setFechaHoraFin(proximoViernes.atTime(10, 30));

        ofertaViernes = new OfertaMedica();
        ofertaViernes.setIdOferta(501);
        ofertaViernes.setEspecialidad(especialidadCardiologia);
        ofertaViernes.setMedico(medicoCardiologo);
        ofertaViernes.setHorario(horarioViernes);
        ofertaViernes.setConsultorio("C-101");
        ofertaViernes.setEstado("Disponible");

        // Horario para semana siguiente (+8 días)
        LocalDate lunesSiguiente = proximoViernes.plusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Horario horarioSiguiente = new Horario();
        horarioSiguiente.setIdHorario(202);
        horarioSiguiente.setFechaHoraInicio(lunesSiguiente.atTime(9, 0));
        horarioSiguiente.setFechaHoraFin(lunesSiguiente.atTime(9, 30));

        ofertaSemanaSiguiente = new OfertaMedica();
        ofertaSemanaSiguiente.setIdOferta(502);
        ofertaSemanaSiguiente.setEspecialidad(especialidadCardiologia);
        ofertaSemanaSiguiente.setMedico(medicoCardiologo);
        ofertaSemanaSiguiente.setHorario(horarioSiguiente);
        ofertaSemanaSiguiente.setConsultorio("C-101");
        ofertaSemanaSiguiente.setEstado("Disponible");
    }

    @Nested
    @DisplayName("CP-MED-01: Escenarios de Verificación de Regla de Negocio RF-04")
    class EscenariosReglaNegocioRF04 {

        @Test
        @DisplayName("CP-MED-01-A [FALLO ESPERADO / RECHAZO]: Bloquear intento de agendar 2da cita en la misma semana para la misma especialidad")
        void cpMed01_DebeBloquearSegundaCitaMismaSemana() {
            // Arrange (GIVEN):
            // La oferta existe y está Disponible
            when(ofertaMedicaDAO.findByIdConDetalles(501)).thenReturn(Optional.of(ofertaViernes));

            // Simular que el paciente ya tiene 1 cita activa registrada en la semana para Cardiología
            when(reservaCitaDAO.countCitasActivasSemanaPorEspecialidad(
                    eq("45892134"),
                    eq(1),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )).thenReturn(1L);

            // Act & Assert (WHEN & THEN):
            assertThatThrownBy(() -> citaService.reservarCita(pacienteAsegurado, 501))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ya cuenta con una cita activa para la especialidad Cardiología en la misma semana");

            // Evidencia de integridad:
            // 1. La oferta médica debe PERMANECER "Disponible" (no se debe consumir el cupo)
            assertThat(ofertaViernes.getEstado()).isEqualTo("Disponible");

            // 2. No se debe haber invocado actualización de oferta médica en la persistencia
            verify(ofertaMedicaDAO, never()).update(any(OfertaMedica.class));

            // 3. No se debe guardar ninguna reserva en base de datos
            verify(reservaCitaDAO, never()).save(any(ReservaCita.class));

            // 4. No se debe despachar ninguna notificación al paciente
            verify(notificacionService, never()).enviarNotificacionConfirmacion(any(ReservaCita.class));
        }

        @Test
        @DisplayName("CP-MED-01-B [ÉXITO PERMITIDO]: Permitir agendar cita para la misma especialidad en semanas distintas")
        void cpMed01_DebePermitirCitaEnSemanaSiguiente() {
            // Arrange (GIVEN):
            when(ofertaMedicaDAO.findByIdConDetalles(502)).thenReturn(Optional.of(ofertaSemanaSiguiente));

            // Para la semana siguiente, el paciente tiene 0 citas activas
            when(reservaCitaDAO.countCitasActivasSemanaPorEspecialidad(
                    eq("45892134"),
                    eq(1),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )).thenReturn(0L);

            when(reservaCitaDAO.save(any(ReservaCita.class))).thenAnswer(inv -> {
                ReservaCita r = inv.getArgument(0);
                r.setIdReserva(999);
                return r;
            });

            // Act (WHEN):
            ReservaCita reservaConfirmada = citaService.reservarCita(pacienteAsegurado, 502);

            // Assert (THEN):
            assertThat(reservaConfirmada).isNotNull();
            assertThat(reservaConfirmada.getEstado()).isEqualTo("Confirmada");
            assertThat(reservaConfirmada.getCodigoTicket()).startsWith("MG-");
            assertThat(ofertaSemanaSiguiente.getEstado()).isEqualTo("Reservado");

            // Verificaciones de efectos colaterales auditables
            verify(ofertaMedicaDAO).update(ofertaSemanaSiguiente);
            verify(reservaCitaDAO).save(any(ReservaCita.class));
            verify(notificacionService).enviarNotificacionConfirmacion(reservaConfirmada);
            verify(bitacoraDAO).save(any(BitacoraEvento.class));
        }

        @Test
        @DisplayName("CP-MED-01-C [ÉXITO PERMITIDO]: Permitir agendar en la misma semana si corresponde a especialidades diferentes")
        void cpMed01_DebePermitirCitaMismaSemanaDiferenteEspecialidad() {
            // Cambiar oferta a Odontología (ID = 2)
            ofertaViernes.setEspecialidad(especialidadOdontologia);

            when(ofertaMedicaDAO.findByIdConDetalles(501)).thenReturn(Optional.of(ofertaViernes));

            // En Odontología tiene 0 citas activas esta semana
            when(reservaCitaDAO.countCitasActivasSemanaPorEspecialidad(
                    eq("45892134"),
                    eq(2),
                    any(LocalDateTime.class),
                    any(LocalDateTime.class)
            )).thenReturn(0L);

            when(reservaCitaDAO.save(any(ReservaCita.class))).thenAnswer(inv -> {
                ReservaCita r = inv.getArgument(0);
                r.setIdReserva(1001);
                return r;
            });

            // Act:
            ReservaCita reserva = citaService.reservarCita(pacienteAsegurado, 501);

            // Assert:
            assertThat(reserva).isNotNull();
            assertThat(reserva.getEstado()).isEqualTo("Confirmada");
            assertThat(ofertaViernes.getEstado()).isEqualTo("Reservado");
            verify(reservaCitaDAO).save(any(ReservaCita.class));
        }

        @Test
        @DisplayName("CP-MED-01-D [SEGURIDAD DEFENSIVA]: Validación de precondiciones y parámetros nulos (Guava Preconditions)")
        void cpMed01_DebeFallarConPrecondicionInvalida() {
            assertThatThrownBy(() -> citaService.reservarCita(null, 501))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("El paciente no puede ser nulo");

            assertThatThrownBy(() -> citaService.reservarCita(pacienteAsegurado, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("El ID de la oferta no puede ser nulo");
        }
    }
}
