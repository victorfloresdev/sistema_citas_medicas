package com.medigestion.controller.web;

import com.medigestion.dao.OfertaMedicaDAO;
import com.medigestion.dao.ReservaCitaDAO;
import com.medigestion.service.AdminService.DashboardMetricas;
import com.medigestion.model.OfertaMedica;
import com.medigestion.model.ReservaCita;
import com.medigestion.service.AdminService;
import com.medigestion.service.CitaService;
import com.medigestion.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * Controlador para el Panel de Administración y Analítica (BPM-4 / Hito 80%).
 * Permite visualizar métricas gerenciales de afluencia y descargar reportes oficiales en Excel nativo (.xlsx) con Apache POI.
 */
@Controller
@RequestMapping("/admin")
public class AdminWebController {

    private final AdminService adminService;
    private final ReporteService reporteService;
    private final ReservaCitaDAO reservaCitaDAO;
    private final OfertaMedicaDAO ofertaMedicaDAO;
    private final CitaService citaService;

    @Autowired
    public AdminWebController(
            AdminService adminService,
            ReporteService reporteService,
            ReservaCitaDAO reservaCitaDAO,
            OfertaMedicaDAO ofertaMedicaDAO,
            CitaService citaService
    ) {
        this.adminService = adminService;
        this.reporteService = reporteService;
        this.reservaCitaDAO = reservaCitaDAO;
        this.ofertaMedicaDAO = ofertaMedicaDAO;
        this.citaService = citaService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardMetricas metricas = adminService.obtenerMetricasDashboard();
        List<OfertaMedica> ofertas = ofertaMedicaDAO.findAllConDetalles();
        List<ReservaCita> citas = reservaCitaDAO.findAll();

        model.addAttribute("metricas", metricas);
        model.addAttribute("ofertas", ofertas);
        model.addAttribute("citas", citas);

        return "admin/dashboard-bi";
    }

    @GetMapping("/reporte-citas/excel")
    public ResponseEntity<byte[]> descargarReporteCitas() throws IOException {
        List<ReservaCita> citas = reservaCitaDAO.findAll();
        File tempFile = File.createTempFile("Reporte_Citas_", ".xlsx");
        try {
            reporteService.exportarReporteCitasExcel(citas, tempFile.getAbsolutePath());
            byte[] contenido = Files.readAllBytes(tempFile.toPath());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte_Citas_Medicas.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(contenido);
        } finally {
            tempFile.delete();
        }
    }

    @GetMapping("/reporte-demanda/excel")
    public ResponseEntity<byte[]> descargarReporteDemanda() throws IOException {
        int afluencia = citaService.calcularAfluenciaActual();
        File tempFile = File.createTempFile("Reporte_Demanda_", ".xlsx");
        try {
            reporteService.exportarReporteDemandaExcel(afluencia, tempFile.getAbsolutePath());
            byte[] contenido = Files.readAllBytes(tempFile.toPath());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte_Demanda_Afluencia.xlsx\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(contenido);
        } finally {
            tempFile.delete();
        }
    }
}
