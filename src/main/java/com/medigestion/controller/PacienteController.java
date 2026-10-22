package com.medigestion.controller;

import com.medigestion.model.*;
import com.medigestion.service.CitaService;
import com.medigestion.service.PacienteService;
import com.medigestion.util.ConsoleUI;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de consola para el flujo del Paciente (BPMN-1, BPMN-2, Pantallas 1 a 4).
 */
public class PacienteController {

    private final PacienteService pacienteService = new PacienteService();
    private final CitaService citaService = new CitaService();
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void iniciarFlujoPaciente() {
        ConsoleUI.limpiarPantalla();
        ConsoleUI.mostrarEncabezado("MEDIGESTION - PORTAL DEL PACIENTE");
        System.out.println("  [Paso 1 de 4] Identidad del Asegurado");
        ConsoleUI.mostrarSeparador();

        String dni = ConsoleUI.leerDni();
        Optional<Paciente> pacienteOpt = pacienteService.identificarPaciente(dni);

        Paciente pacienteActual;
        if (pacienteOpt.isPresent()) {
            pacienteActual = pacienteOpt.get();
            ConsoleUI.mostrarExito("Paciente identificado: " + pacienteActual.getNombreCompleto());
        } else {
            ConsoleUI.mostrarAdvertencia("DNI no registrado en el sistema. Iniciando registro ágil...");
            pacienteActual = registrarNuevoPaciente(dni);
        }

        ConsoleUI.pausar();
        menuPrincipalPaciente(pacienteActual);
    }

    private Paciente registrarNuevoPaciente(String dni) {
        ConsoleUI.mostrarSubtitulo("Formulario de Registro Ágil de Paciente");
        String nombres = ConsoleUI.leerTextoObligatorio("Nombres");
        String apellidos = ConsoleUI.leerTextoObligatorio("Apellidos");
        
        short edad = 0;
        while (true) {
            try {
                edad = Short.parseShort(ConsoleUI.leerTextoObligatorio("Edad (0-120)"));
                if (edad >= 0 && edad <= 120) break;
                ConsoleUI.mostrarError("Edad fuera de rango.");
            } catch (NumberFormatException e) {
                ConsoleUI.mostrarError("Ingrese un número válido para la edad.");
            }
        }

        String sexo;
        while (true) {
            sexo = ConsoleUI.leerTextoObligatorio("Sexo (HOMBRE / MUJER)").toUpperCase();
            if (sexo.equals("HOMBRE") || sexo.equals("MUJER")) break;
            ConsoleUI.mostrarError("Ingrese HOMBRE o MUJER.");
        }

        String telefono = ConsoleUI.leerTelefono();
        String email = ConsoleUI.leerEmail();
        String direccion = ConsoleUI.leerTexto("Dirección (opcional)");
        String password = ConsoleUI.leerTextoObligatorio("Cree una contraseña para su cuenta");

        Paciente creado = pacienteService.registrarPaciente(dni, nombres, apellidos, edad, sexo, telefono, email, password, direccion);
        ConsoleUI.mostrarExito("¡Registro completado con éxito! Bienvenido(a), " + creado.getNombreCompleto());
        return creado;
    }

    private void menuPrincipalPaciente(Paciente paciente) {
        boolean salir = false;
        while (!salir) {
            ConsoleUI.limpiarPantalla();
            ConsoleUI.mostrarEncabezado("MEDIGESTION - PANEL DE CONTROL (PACIENTE)");
            System.out.println("  Bienvenido: " + paciente.getNombreCompleto() + " | DNI: " + paciente.getDni());
            
            // RF-03: Indicador de afluencia en pantalla principal
            int afluencia = citaService.calcularAfluenciaActual();
            String estadoAfluencia = (afluencia < 40) ? "Bajo" : (afluencia < 75) ? "Moderado" : "Alto";
            System.out.printf("  Nivel de Afluencia del Establecimiento: %d%% [%s]%n", afluencia, estadoAfluencia);
            ConsoleUI.mostrarSeparador();

            ConsoleUI.mostrarMenu("OPCIONES DISPONIBLES",
                    "Nueva Reserva de Cita Médica",
                    "Consultar / Cancelar Mis Citas Agendadas",
                    "Consultar Disponibilidad de Cartelera",
                    "Cerrar Sesión");

            int opcion = ConsoleUI.leerOpcion("Seleccione una opción", 1, 4);
            switch (opcion) {
                case 1 -> flujoReservaCita(paciente);
                case 2 -> flujoMisCitas(paciente);
                case 3 -> mostrarCarteleraDisponibilidad();
                case 4 -> salir = true;
            }
        }
    }

    private void flujoReservaCita(Paciente paciente) {
        ConsoleUI.limpiarPantalla();
        ConsoleUI.mostrarEncabezado("NUEVA CITA MÉDICA - SELECCIÓN DE ESPECIALIDAD");
        System.out.println("  [Paso 2 de 4] Especialidad");
        ConsoleUI.mostrarSeparador();

        List<Especialidad> especialidades = citaService.listarEspecialidades();
        if (especialidades.isEmpty()) {
            ConsoleUI.mostrarError("No hay especialidades registradas en este momento.");
            ConsoleUI.pausar();
            return;
        }

        for (int i = 0; i < especialidades.size(); i++) {
            System.out.printf("    [%d] %s%n", i + 1, especialidades.get(i).getNombreEspecialidad());
        }
        System.out.printf("    [%d] Volver al menú anterior%n", especialidades.size() + 1);

        int seleccionEsp = ConsoleUI.leerOpcion("Seleccione la Especialidad deseada", 1, especialidades.size() + 1);
        if (seleccionEsp == especialidades.size() + 1) return;

        Especialidad espElegida = especialidades.get(seleccionEsp - 1);

        // Paso 3: Horarios y Médicos disponibles
        ConsoleUI.limpiarPantalla();
        ConsoleUI.mostrarEncabezado("MÉDICOS Y TURNOS DISPONIBLES - " + espElegida.getNombreEspecialidad().toUpperCase());
        System.out.println("  [Paso 3 de 4] Horario y Médico Tratante");
        ConsoleUI.mostrarSeparador();

        List<OfertaMedica> ofertas = citaService.buscarOfertasDisponibles(espElegida.getIdEspecialidad(), paciente);

        if (ofertas.isEmpty()) {
            ConsoleUI.mostrarAdvertencia("No hay cupos disponibles en " + espElegida.getNombreEspecialidad() + 
                    " dentro de los próximos 30 días calendario.");
            ConsoleUI.mostrarInfo("Su interés ha sido registrado en 'Demanda No Atendida' para ampliar la oferta médica.");
            ConsoleUI.pausar();
            return;
        }

        String[] cabeceras = {"N°", "Médico Tratante", "Consultorio", "Fecha y Hora Inicio", "Fecha y Hora Fin"};
        String[][] datos = new String[ofertas.size()][5];

        for (int i = 0; i < ofertas.size(); i++) {
            OfertaMedica om = ofertas.get(i);
            datos[i][0] = String.valueOf(i + 1);
            datos[i][1] = om.getMedico().getNombreCompleto();
            datos[i][2] = om.getConsultorio();
            datos[i][3] = om.getHorario().getFechaHoraInicio().format(FMT_HORA);
            datos[i][4] = om.getHorario().getFechaHoraFin().format(FMT_HORA);
        }
        ConsoleUI.mostrarTabla(cabeceras, datos);

        System.out.printf("%n    [%d] Cancelar y volver%n", ofertas.size() + 1);
        int opcHorario = ConsoleUI.leerOpcion("Seleccione el horario de su preferencia", 1, ofertas.size() + 1);
        if (opcHorario == ofertas.size() + 1) return;

        OfertaMedica ofertaElegida = ofertas.get(opcHorario - 1);

        // Paso 4: Confirmación
        ConsoleUI.limpiarPantalla();
        ConsoleUI.mostrarEncabezado("CONFIRMACIÓN DE CITA MÉDICA");
        System.out.println("  [Paso 4 de 4] Confirmar Reserva");
        ConsoleUI.mostrarSeparador();
        System.out.println("  Especialidad:  " + espElegida.getNombreEspecialidad());
        System.out.println("  Médico:        " + ofertaElegida.getMedico().getNombreCompleto());
        System.out.println("  Consultorio:   " + ofertaElegida.getConsultorio());
        System.out.println("  Fecha y Hora:  " + ofertaElegida.getHorario().getFechaHoraInicio().format(FMT_HORA));
        System.out.println("  Paciente:      " + paciente.getNombreCompleto());
        ConsoleUI.mostrarSeparador();

        if (ConsoleUI.confirmar("¿Desea confirmar esta reserva de cita médica?")) {
            try {
                ReservaCita reserva = citaService.reservarCita(paciente, ofertaElegida.getIdOferta());
                
                // Mostrar pantalla idéntica a Prototipo cita2-mg.jpeg
                ConsoleUI.limpiarPantalla();
                ConsoleUI.mostrarEncabezado("¡CITA CONFIRMADA EXITOSAMENTE! (TICKET DIGITAL)");
                System.out.println("  ┌──────────────────────────────────────────────────────────┐");
                System.out.printf("  │  CÓDIGO DE RESERVA :  %-33s  │%n", reserva.getCodigoTicket());
                System.out.println("  ├──────────────────────────────────────────────────────────┤");
                System.out.printf("  │  Médico        : %-37s │%n", ofertaElegida.getMedico().getNombreCompleto());
                System.out.printf("  │  Especialidad  : %-37s │%n", espElegida.getNombreEspecialidad());
                System.out.printf("  │  Consultorio   : %-37s │%n", ofertaElegida.getConsultorio());
                System.out.printf("  │  Fecha y Hora  : %-37s │%n", reserva.getOfertaMedica().getHorario().getFechaHoraInicio().format(FMT_HORA));
                System.out.printf("  │  Turno         : %-37s │%n", reserva.getTurno());
                System.out.printf("  │  Ubicación     : %-37s │%n", "Hospital Principal MediGestión");
                System.out.println("  ├──────────────────────────────────────────────────────────┤");
                System.out.println("  │  INSTRUCCIONES IMPORTANTES:                              │");
                System.out.println("  │  • Por favor, llegue 15 minutos antes de su turno.       │");
                System.out.println("  │  • Traiga su DNI físico y carné de asegurado.            │");
                System.out.println("  │  • Notificación enviada al canal Telegram registrado.    │");
                System.out.println("  └──────────────────────────────────────────────────────────┘");
                ConsoleUI.mostrarExito("Comprobante generado y registrado en la base de datos.");
            } catch (Exception e) {
                ConsoleUI.mostrarError("No se pudo completar la reserva: " + e.getMessage());
            }
        } else {
            ConsoleUI.mostrarInfo("Operación cancelada por el usuario.");
        }
        ConsoleUI.pausar();
    }

    private void flujoMisCitas(Paciente paciente) {
        ConsoleUI.limpiarPantalla();
        ConsoleUI.mostrarEncabezado("MIS CITAS AGENDADAS");
        List<ReservaCita> misCitas = citaService.listarCitasPorPaciente(paciente.getDni());

        if (misCitas.isEmpty()) {
            ConsoleUI.mostrarInfo("Usted no tiene citas agendadas actualmente.");
            ConsoleUI.pausar();
            return;
        }

        String[] cabeceras = {"N°", "Ticket", "Especialidad", "Médico", "Fecha/Hora", "Estado"};
        String[][] datos = new String[misCitas.size()][6];

        for (int i = 0; i < misCitas.size(); i++) {
            ReservaCita rc = misCitas.get(i);
            datos[i][0] = String.valueOf(i + 1);
            datos[i][1] = rc.getCodigoTicket();
            datos[i][2] = rc.getOfertaMedica().getEspecialidad().getNombreEspecialidad();
            datos[i][3] = rc.getOfertaMedica().getMedico().getNombreCompleto();
            datos[i][4] = rc.getOfertaMedica().getHorario().getFechaHoraInicio().format(FMT_HORA);
            datos[i][5] = rc.getEstado();
        }
        ConsoleUI.mostrarTabla(cabeceras, datos);

        ConsoleUI.mostrarSeparador();
        System.out.println("    [1] Cancelar una cita médica");
        System.out.println("    [2] Volver al panel de control");
        int opc = ConsoleUI.leerOpcion("Seleccione una opción", 1, 2);

        if (opc == 1) {
            int numCita = ConsoleUI.leerOpcion("Indique el N° de la cita que desea cancelar", 1, misCitas.size());
            ReservaCita elegida = misCitas.get(numCita - 1);

            if ("Cancelada".equalsIgnoreCase(elegida.getEstado())) {
                ConsoleUI.mostrarAdvertencia("Esa cita ya se encuentra en estado Cancelada.");
            } else {
                String motivo = ConsoleUI.leerTextoObligatorio("Motivo de la cancelación");
                if (ConsoleUI.confirmar("¿Confirma la cancelación de la cita " + elegida.getCodigoTicket() + "?")) {
                    try {
                        // Canal WEB: aplica regla de 1 hora de anticipación
                        citaService.cancelarCita(elegida.getIdReserva(), "WEB", motivo);
                        ConsoleUI.mostrarExito("Cita cancelada con éxito y cupo reintegrado a la oferta médica.");
                    } catch (Exception e) {
                        ConsoleUI.mostrarError("Error al cancelar: " + e.getMessage());
                    }
                }
            }
            ConsoleUI.pausar();
        }
    }

    private void mostrarCarteleraDisponibilidad() {
        ConsoleUI.limpiarPantalla();
        ConsoleUI.mostrarEncabezado("CARTELERA DE HORARIOS Y ESPECIALIDADES (RF-03)");
        List<Especialidad> especialidades = citaService.listarEspecialidades();

        String[] cabeceras = {"Especialidad", "Estado Disponibilidad", "Próximo Turno"};
        String[][] datos = new String[especialidades.size()][3];

        for (int i = 0; i < especialidades.size(); i++) {
            Especialidad esp = especialidades.get(i);
            List<OfertaMedica> cupos = citaService.buscarOfertasDisponibles(esp.getIdEspecialidad(), null);
            datos[i][0] = esp.getNombreEspecialidad();
            if (cupos.isEmpty()) {
                datos[i][1] = "Agotado";
                datos[i][2] = "Sin cupos";
            } else {
                datos[i][1] = "Disponible (" + cupos.size() + " cupos)";
                datos[i][2] = cupos.get(0).getHorario().getFechaHoraInicio().format(FMT_HORA);
            }
        }
        ConsoleUI.mostrarTabla(cabeceras, datos);
        ConsoleUI.pausar();
    }
}
