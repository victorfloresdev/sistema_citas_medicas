package com.medigestion;

import com.medigestion.controller.AdminController;
import com.medigestion.controller.MedicoController;
import com.medigestion.controller.PacienteController;
import com.medigestion.util.ConsoleUI;
import com.medigestion.util.DataSeeder;
import com.medigestion.util.HibernateUtil;

/**
 * Clase principal de inicio para MediGestión - Sistema de Citas Médicas.
 */
public class App {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("          MEDIGESTION - SISTEMA DE GESTIÓN DE CITAS MÉDICAS       ");
        System.out.println("==================================================================");
        System.out.println("Conectando con la base de datos e inicializando componentes Hibernate...");

        try {
            // Inicializar datos maestros de demostración
            DataSeeder.seed();
        } catch (Exception e) {
            System.err.println("Advertencia de inicialización: " + e.getMessage());
            System.err.println("Asegúrese de tener PostgreSQL activo en localhost:5432 con la base 'medigestion'.");
        }

        PacienteController pacienteController = new PacienteController();
        MedicoController medicoController = new MedicoController();
        AdminController adminController = new AdminController();

        boolean salir = false;
        while (!salir) {
            ConsoleUI.limpiarPantalla();
            ConsoleUI.mostrarEncabezado("MEDIGESTION - PORTAL PRINCIPAL");
            ConsoleUI.mostrarMenu("SELECCIONE EL ROL DE ACCESO",
                    "Portal del Paciente (Identificación DNI, Reserva y Cancelación)",
                    "Estación Médica / Consultorio (Llamado y Atención de Pacientes)",
                    "Panel de Administración Hospitalaria (Dashboard y Agendas)",
                    "Salir del Sistema");

            int opcion = ConsoleUI.leerOpcion("Seleccione una opción", 1, 4);
            switch (opcion) {
                case 1 -> pacienteController.iniciarFlujoPaciente();
                case 2 -> medicoController.iniciarFlujoMedico();
                case 3 -> adminController.iniciarFlujoAdmin();
                case 4 -> {
                    ConsoleUI.mostrarInfo("Finalizando sesión y liberando recursos...");
                    HibernateUtil.shutdown();
                    ConsoleUI.mostrarExito("¡Gracias por utilizar MediGestión!");
                    salir = true;
                }
            }
        }
    }
}
