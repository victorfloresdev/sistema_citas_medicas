package com.medigestion.controller;

import com.medigestion.model.ReservaCita;
import com.medigestion.service.AtencionService;
import com.medigestion.service.CitaService;
import com.medigestion.util.ConsoleUI;

import java.util.Optional;

/**
 * Controlador para la interfaz del Médico en consultorio (BPMN-3, RF-13).
 */
public class MedicoController {

    private final AtencionService atencionService = new AtencionService();
    private final CitaService citaService = new CitaService();

    public void iniciarFlujoMedico() {
        boolean salir = false;
        while (!salir) {
            ConsoleUI.limpiarPantalla();
            ConsoleUI.mostrarEncabezado("MEDIGESTION - ESTACIÓN MÉDICA / CONSULTORIO");
            ConsoleUI.mostrarMenu("GESTIÓN DE ATENCIÓN DE PACIENTES",
                    "Llamar Paciente a Consultorio (Actualizar Sala de Espera)",
                    "Registrar Paciente Atendido (Cita Completada)",
                    "Marcar Paciente como Ausente",
                    "Buscar Cita por Ticket de Reserva",
                    "Regresar al Menú Principal");

            int opc = ConsoleUI.leerOpcion("Seleccione una opción", 1, 5);
            switch (opc) {
                case 1 -> flujoLlamarPaciente();
                case 2 -> flujoCompletarCita();
                case 3 -> flujoMarcarAusente();
                case 4 -> flujoBuscarTicket();
                case 5 -> salir = true;
            }
        }
    }

    private void flujoLlamarPaciente() {
        ConsoleUI.mostrarSubtitulo("Llamar Paciente (BPMN-3)");
        String ticket = ConsoleUI.leerTextoObligatorio("Ingrese el código del ticket de la cita a llamar");
        Optional<ReservaCita> citaOpt = citaService.buscarPorTicket(ticket);

        if (citaOpt.isPresent()) {
            ReservaCita r = citaOpt.get();
            try {
                ReservaCita llamada = atencionService.llamarPaciente(r.getIdReserva());
                ConsoleUI.mostrarExito("¡PACIENTE LLAMADO A PANTALLA DE SALA DE ESPERA!");
                System.out.println("  Paciente:     " + llamada.getPaciente().getNombreCompleto());
                System.out.println("  Consultorio:  " + llamada.getOfertaMedica().getConsultorio());
                System.out.println("  Hora llamado: " + llamada.getFechaLlamado());
            } catch (Exception e) {
                ConsoleUI.mostrarError("Error al procesar llamado: " + e.getMessage());
            }
        } else {
            ConsoleUI.mostrarError("Ticket no encontrado en la base de datos.");
        }
        ConsoleUI.pausar();
    }

    private void flujoCompletarCita() {
        ConsoleUI.mostrarSubtitulo("Finalizar Atención Médica");
        String ticket = ConsoleUI.leerTextoObligatorio("Ingrese código de ticket a marcar como COMPLETADA");
        Optional<ReservaCita> citaOpt = citaService.buscarPorTicket(ticket);

        if (citaOpt.isPresent()) {
            ReservaCita r = citaOpt.get();
            try {
                atencionService.marcarCompletada(r.getIdReserva());
                ConsoleUI.mostrarExito("Cita marcada como COMPLETADA. Historia de atención cerrada.");
            } catch (Exception e) {
                ConsoleUI.mostrarError("Error: " + e.getMessage());
            }
        } else {
            ConsoleUI.mostrarError("Ticket no encontrado.");
        }
        ConsoleUI.pausar();
    }

    private void flujoMarcarAusente() {
        ConsoleUI.mostrarSubtitulo("Marcar Paciente Ausente (Tolerancia Vencida)");
        String ticket = ConsoleUI.leerTextoObligatorio("Ingrese código de ticket");
        Optional<ReservaCita> citaOpt = citaService.buscarPorTicket(ticket);

        if (citaOpt.isPresent()) {
            ReservaCita r = citaOpt.get();
            try {
                atencionService.marcarAusente(r.getIdReserva());
                ConsoleUI.mostrarAdvertencia("Cita marcada como AUSENTE. Turno cerrado sin atención.");
            } catch (Exception e) {
                ConsoleUI.mostrarError("Error: " + e.getMessage());
            }
        } else {
            ConsoleUI.mostrarError("Ticket no encontrado.");
        }
        ConsoleUI.pausar();
    }

    private void flujoBuscarTicket() {
        ConsoleUI.mostrarSubtitulo("Consultar Estado de Cita por Ticket");
        String ticket = ConsoleUI.leerTextoObligatorio("Código de ticket (ej. MG-XXXX-XXXX)");
        Optional<ReservaCita> citaOpt = citaService.buscarPorTicket(ticket);

        if (citaOpt.isPresent()) {
            ReservaCita r = citaOpt.get();
            System.out.println();
            System.out.println("  ┌──────────────────────────────────────────────────────────┐");
            System.out.printf("  │  Ticket      : %-41s │%n", r.getCodigoTicket());
            System.out.printf("  │  Estado      : %-41s │%n", r.getEstado());
            System.out.printf("  │  Paciente    : %-41s │%n", r.getPaciente().getNombreCompleto());
            System.out.printf("  │  DNI         : %-41s │%n", r.getPaciente().getDni());
            System.out.printf("  │  Especialidad: %-41s │%n", r.getOfertaMedica().getEspecialidad().getNombreEspecialidad());
            System.out.printf("  │  Médico      : %-41s │%n", r.getOfertaMedica().getMedico().getNombreCompleto());
            System.out.printf("  │  Consultorio : %-41s │%n", r.getOfertaMedica().getConsultorio());
            System.out.printf("  │  Turno       : %-41s │%n", r.getTurno());
            System.out.println("  └──────────────────────────────────────────────────────────┘");
        } else {
            ConsoleUI.mostrarError("No se encontró ninguna cita con el código indicado.");
        }
        ConsoleUI.pausar();
    }
}
