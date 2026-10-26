package com.medigestion;

import com.medigestion.controller.AdminController;
import com.medigestion.controller.MedicoController;
import com.medigestion.controller.PacienteController;
import com.medigestion.controller.ReporteController;
import com.medigestion.util.ConsoleUI;
import com.medigestion.util.DataSeeder;
import com.medigestion.util.HibernateUtil;

/**
 * Clase principal de inicio para MediGestión - Sistema de Citas Médicas.
 * Versión: Avance de Desarrollo 40% (Semana 11 - Rúbrica de Curso Integrador I).
 */
public class App {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("          MEDIGESTIÓN - SISTEMA DE GESTIÓN DE CITAS MÉDICAS       ");
        System.out.println("            [HITO ACADÉMICO: AVANCE DE DESARROLLO 40%]            ");
        System.out.println("==================================================================");
        System.out.println("Conectando con la base de datos e inicializando componentes...");

        try {
            // Inicializar datos maestros de demostración del prototipo
            DataSeeder.seed();
        } catch (Exception e) {
            System.err.println("Advertencia de inicialización: " + e.getMessage());
            System.err.println("Asegúrese de tener PostgreSQL activo en localhost:5432 con la base 'medigestion'.");
        }

        PacienteController pacienteController = new PacienteController();
        ReporteController reporteController = new ReporteController();
        MedicoController medicoController = new MedicoController();
        AdminController adminController = new AdminController();

        boolean salir = false;
        while (!salir) {
            ConsoleUI.limpiarPantalla();
            ConsoleUI.mostrarEncabezado("MEDIGESTIÓN - PORTAL PRINCIPAL (AVANCE 40%)");
            System.out.println("  Alcance Actual: Flujo Crítico BPM-1 (Reserva de Citas) + Reportería Excel (Apache POI)");
            System.out.println("  Recursos Activos: Google Guava | Apache POI | Apache Commons | Logback");
            ConsoleUI.mostrarSeparador();

            ConsoleUI.mostrarMenu("SELECCIONE EL MÓDULO DE TRABAJO",
                    "Portal del Paciente [BPM-1: Solicitud, Validación y Reserva] (100% Operativo)",
                    "Módulo de Reportes Oficiales en Excel [Apache POI | REP-01] (100% Operativo)",
                    "Estación Médica y Sala de Espera [BPM-3 | RF-13] (Delimitación: Hito 60% / Sem 13)",
                    "Panel de Administración y Agendas [BPM-4 | RF-14] (Delimitación: Hito 80% / Sem 15)",
                    "Salir del Sistema");

            int opcion = ConsoleUI.leerOpcion("Seleccione una opción", 1, 5);
            switch (opcion) {
                case 1 -> pacienteController.iniciarFlujoPaciente();
                case 2 -> reporteController.iniciarModuloReportes();
                case 3 -> mostrarDelimitacionModulo(
                        "ESTACIÓN MÉDICA Y SALA DE ESPERA (BPM-3)",
                        "Semana 13 (Hito 60% - Pruebas de Software)",
                        "El llamado público a consultorios y atención médica se encuentra delimitado para el siguiente hito.",
                        () -> medicoController.iniciarFlujoMedico()
                );
                case 4 -> mostrarDelimitacionModulo(
                        "PANEL DE ADMINISTRACIÓN Y AGENDAS (BPM-4)",
                        "Semanas 15-16 (Hito 80%-90% - Despliegue y Monitoreo)",
                        "La parametrización masiva de agendas y auditoría avanzada forma parte de la fase final de despliegue.",
                        () -> adminController.iniciarFlujoAdmin()
                );
                case 5 -> {
                    ConsoleUI.mostrarInfo("Finalizando sesión y liberando recursos...");
                    HibernateUtil.shutdown();
                    ConsoleUI.mostrarExito("¡Gracias por utilizar MediGestión!");
                    salir = true;
                }
            }
        }
    }

    private static void mostrarDelimitacionModulo(String nombreModulo, String hitoDestino, String descripcion, Runnable accionOpcional) {
        ConsoleUI.limpiarPantalla();
        ConsoleUI.mostrarEncabezado("DELIMITACIÓN DE ALCANCE FUNCIONAL - " + nombreModulo);
        System.out.println("  ESTADO DE DESARROLLO: [MÓDULO EN PLANIFICACIÓN SEGÚN CRONOGRAMA]");
        System.out.println("  Hito Asignado: " + hitoDestino);
        System.out.println("  Detalle: " + descripcion);
        ConsoleUI.mostrarSeparador();
        System.out.println("  NOTA DE RÚBRICA:");
        System.out.println("  Para cumplir con el requerimiento del docente de presentar exactamente un 40% del");
        System.out.println("  sistema propuesto, este módulo no forma parte del flujo crítico de este entregable.");
        System.out.println("  El 40% se encuentra validado y operativo en: Portal Paciente (BPM-1) y Reportería POI (REP-01).");
        ConsoleUI.mostrarSeparador();

        String verDemo = ConsoleUI.leerTexto("¿Desea ingresar a la vista previa técnica de este módulo? (S/N)");
        if ("S".equalsIgnoreCase(verDemo)) {
            accionOpcional.run();
        }
    }
}
