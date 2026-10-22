package com.medigestion.controller;

import com.medigestion.model.Medico;
import com.medigestion.model.OfertaMedica;
import com.medigestion.service.AdminService;
import com.medigestion.util.ConsoleUI;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador para la interfaz del Administrador (BPMN-4, RF-14).
 */
public class AdminController {

    private final AdminService adminService = new AdminService();
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void iniciarFlujoAdmin() {
        ConsoleUI.limpiarPantalla();
        ConsoleUI.mostrarEncabezado("MEDIGESTION - PANEL ADMINISTRATIVO");

        String email = ConsoleUI.leerTextoObligatorio("Ingrese correo de Administrador");
        if (!email.toLowerCase().contains("admin")) {
            ConsoleUI.mostrarAdvertencia("Acceso en modo demostración para: " + email);
        }

        boolean salir = false;
        while (!salir) {
            ConsoleUI.limpiarPantalla();
            ConsoleUI.mostrarEncabezado("MEDIGESTION - CONSOLA DE ADMINISTRACIÓN (BPMN-4)");
            ConsoleUI.mostrarMenu("REPORTES Y CONFIGURACIÓN",
                    "Dashboard de Indicadores Operativos",
                    "Ver Listado General de Ofertas Médicas (Agendas)",
                    "Ver Directorio de Médicos y Consultorios",
                    "Cerrar Sesión de Administrador");

            int opc = ConsoleUI.leerOpcion("Seleccione una opción", 1, 4);
            switch (opc) {
                case 1 -> mostrarDashboardMetricas();
                case 2 -> listarOfertasMedicas();
                case 3 -> listarDirectorioMedicos();
                case 4 -> salir = true;
            }
        }
    }

    private void mostrarDashboardMetricas() {
        ConsoleUI.limpiarPantalla();
        ConsoleUI.mostrarEncabezado("DASHBOARD GERENCIAL DE AFLUENCIA Y CAPACIDAD");
        AdminService.DashboardMetricas m = adminService.obtenerMetricasDashboard();

        System.out.println("  ┌──────────────────────────────────────────────────────────┐");
        System.out.printf("  │  Citas Agendadas para Hoy    : %-25d │%n", m.citasHoy);
        System.out.printf("  │  Total de Médicos Activos     : %-25d │%n", m.totalMedicos);
        System.out.printf("  │  Especialidades Disponibles  : %-25d │%n", m.totalEspecialidades);
        System.out.printf("  │  Cupos Totales Programados   : %-25d │%n", m.totalOfertasProgramadas);
        System.out.printf("  │  Cupos Reservados            : %-25d │%n", m.citasReservadas);
        System.out.println("  └──────────────────────────────────────────────────────────┘");
        ConsoleUI.pausar();
    }

    private void listarOfertasMedicas() {
        ConsoleUI.limpiarPantalla();
        ConsoleUI.mostrarEncabezado("OFERTAS MÉDICAS PROGRAMADAS");
        List<OfertaMedica> ofertas = adminService.listarTodasLasOfertas();

        if (ofertas.isEmpty()) {
            ConsoleUI.mostrarInfo("No hay ofertas médicas programadas.");
            ConsoleUI.pausar();
            return;
        }

        String[] cabeceras = {"ID", "Especialidad", "Médico", "Consultorio", "Horario", "Estado"};
        String[][] datos = new String[ofertas.size()][6];

        for (int i = 0; i < ofertas.size(); i++) {
            OfertaMedica o = ofertas.get(i);
            datos[i][0] = String.valueOf(o.getIdOferta());
            datos[i][1] = o.getEspecialidad().getNombreEspecialidad();
            datos[i][2] = o.getMedico().getNombreCompleto();
            datos[i][3] = o.getConsultorio();
            datos[i][4] = o.getHorario().getFechaHoraInicio().format(FMT_HORA);
            datos[i][5] = o.getEstado();
        }
        ConsoleUI.mostrarTabla(cabeceras, datos);
        ConsoleUI.pausar();
    }

    private void listarDirectorioMedicos() {
        ConsoleUI.limpiarPantalla();
        ConsoleUI.mostrarEncabezado("DIRECTORIO MÉDICO");
        List<Medico> medicos = adminService.listarMedicos();

        String[] cabeceras = {"ID", "Médico", "Consultorio", "Email", "Estado"};
        String[][] datos = new String[medicos.size()][5];

        for (int i = 0; i < medicos.size(); i++) {
            Medico m = medicos.get(i);
            datos[i][0] = String.valueOf(m.getIdMedico());
            datos[i][1] = m.getNombreCompleto();
            datos[i][2] = m.getConsultorio();
            datos[i][3] = m.getEmail();
            datos[i][4] = m.getActivo() ? "Activo" : "Inactivo";
        }
        ConsoleUI.mostrarTabla(cabeceras, datos);
        ConsoleUI.pausar();
    }
}
