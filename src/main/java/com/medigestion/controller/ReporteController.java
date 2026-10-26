package com.medigestion.controller;

import com.medigestion.service.CitaService;
import com.medigestion.service.ReporteService;
import com.medigestion.util.ConsoleUI;

import java.io.File;

/**
 * Controlador de consola para la generación y exportación de reportes con Apache POI (REP-01).
 * Aplica principios de MVC (separación de presentación respecto a ReporteService).
 */
public class ReporteController {

    private final ReporteService reporteService = new ReporteService();
    private final CitaService citaService = new CitaService();

    public void iniciarModuloReportes() {
        boolean volver = false;
        while (!volver) {
            ConsoleUI.limpiarPantalla();
            ConsoleUI.mostrarEncabezado("MEDIGESTIÓN - MÓDULO DE REPORTERÍA OFICIAL (APACHE POI)");
            System.out.println("  [REP-01] Generación de libros Excel (.xlsx) con estilos y formato institucional");
            ConsoleUI.mostrarSeparador();

            ConsoleUI.mostrarMenu("SELECCIONE EL REPORTE A GENERAR",
                    "Exportar Reporte de Citas Médicas Programadas [R01]",
                    "Exportar Reporte de Demanda No Atendida y Afluencia [R02, R03]",
                    "Exportar Ambos Reportes Ejecutivos",
                    "Volver al Menú Principal");

            int op = ConsoleUI.leerOpcion("Seleccione una opción", 1, 4);
            switch (op) {
                case 1 -> generarReporteCitas();
                case 2 -> generarReporteDemanda();
                case 3 -> {
                    generarReporteCitas();
                    generarReporteDemanda();
                }
                case 4 -> volver = true;
            }
            if (!volver) {
                ConsoleUI.pausar();
            }
        }
    }

    private void generarReporteCitas() {
        try {
            String ruta = "reportes/Reporte_Citas_Medicas.xlsx";
            ConsoleUI.mostrarInfo("Construyendo libro Excel con Apache POI...");
            File f = reporteService.exportarReporteCitasExcel(ruta);
            ConsoleUI.mostrarExito("¡Reporte generado con éxito en: " + f.getAbsolutePath());
            System.out.println("  Tamaño del archivo: " + f.length() + " bytes");
            System.out.println("  Formato: Microsoft Excel OpenXML (.xlsx)");
            System.out.println("  Contenido: Cabeceras corporativas, registros tabulares con cebra y totales calculados.");
        } catch (Exception e) {
            ConsoleUI.mostrarError("Error al generar reporte de citas: " + e.getMessage());
        }
    }

    private void generarReporteDemanda() {
        try {
            String ruta = "reportes/Reporte_Demanda_Afluencia.xlsx";
            int afluencia = citaService.calcularAfluenciaActual();
            ConsoleUI.mostrarInfo("Construyendo libro Excel de demanda no atendida...");
            File f = reporteService.exportarReporteDemandaExcel(afluencia, ruta);
            ConsoleUI.mostrarExito("¡Reporte generado con éxito en: " + f.getAbsolutePath());
            System.out.println("  Tamaño del archivo: " + f.length() + " bytes");
            System.out.println("  Formato: Microsoft Excel OpenXML (.xlsx)");
        } catch (Exception e) {
            ConsoleUI.mostrarError("Error al generar reporte de demanda: " + e.getMessage());
        }
    }
}
