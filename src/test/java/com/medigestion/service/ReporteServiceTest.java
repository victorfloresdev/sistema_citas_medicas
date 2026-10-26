package com.medigestion.service;

import com.medigestion.dao.IDemandaNoAtendidaDAO;
import com.medigestion.dao.IReservaCitaDAO;
import com.medigestion.model.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para ReporteService demostrando el funcionamiento de Apache POI (REP-01).
 * Valida la creación física de libros Excel (.xlsx), hojas, cabeceras y persistencia en disco.
 */
@ExtendWith(MockitoExtension.class)
public class ReporteServiceTest {

    @Mock
    private IReservaCitaDAO reservaCitaDAO;

    @Mock
    private IDemandaNoAtendidaDAO demandaDAO;

    private ReporteService reporteService;

    @BeforeEach
    void setUp() {
        reporteService = new ReporteService(reservaCitaDAO, demandaDAO);
    }

    @Test
    @DisplayName("Apache POI: Generar y verificar estructura del libro Excel de Citas Médicas")
    void testExportarReporteCitasExcel_GeneracionValida() throws IOException {
        // Preparar datos de prueba
        List<ReservaCita> citas = new ArrayList<>();

        Paciente p = new Paciente();
        p.setDni("45892134");
        p.setNombres("Carlos");
        p.setApellidos("Mendoza Rivera");

        Especialidad esp = new Especialidad();
        esp.setNombreEspecialidad("Cardiología");

        Medico m = new Medico();
        m.setNombres("Sarah");
        m.setApellidos("Jenkins");
        m.setConsultorio("C-101");

        Horario h = new Horario();
        h.setFechaHoraInicio(LocalDateTime.of(2026, 11, 15, 10, 0));

        OfertaMedica om = new OfertaMedica();
        om.setEspecialidad(esp);
        om.setMedico(m);
        om.setHorario(h);

        ReservaCita cita1 = new ReservaCita();
        cita1.setIdReserva(101);
        cita1.setCodigoTicket("MG-TEST-101");
        cita1.setEstado("Confirmada");
        cita1.setTurno("Mañana");
        cita1.setPaciente(p);
        cita1.setOfertaMedica(om);
        citas.add(cita1);

        String rutaTest = "target/test-reports/Reporte_Test_Citas.xlsx";
        File archivo = reporteService.exportarReporteCitasExcel(citas, rutaTest);

        // Verificaciones
        assertThat(archivo).exists();
        assertThat(archivo.length()).isGreaterThan(0);

        try (Workbook wb = WorkbookFactory.create(archivo)) {
            Sheet sheet = wb.getSheet("Citas Médicas");
            assertThat(sheet).isNotNull();

            // Validar título
            Row rTitulo = sheet.getRow(0);
            assertThat(rTitulo.getCell(0).getStringCellValue()).contains("MEDIGESTIÓN");

            // Validar cabeceras en fila 3
            Row rCabecera = sheet.getRow(3);
            assertThat(rCabecera.getCell(0).getStringCellValue()).isEqualTo("N°");
            assertThat(rCabecera.getCell(1).getStringCellValue()).isEqualTo("Código Ticket");
            assertThat(rCabecera.getCell(2).getStringCellValue()).isEqualTo("DNI Asegurado");
            assertThat(rCabecera.getCell(3).getStringCellValue()).isEqualTo("Paciente");
            assertThat(rCabecera.getCell(4).getStringCellValue()).isEqualTo("Especialidad");
            assertThat(rCabecera.getCell(5).getStringCellValue()).isEqualTo("Médico Tratante");
            assertThat(rCabecera.getCell(6).getStringCellValue()).isEqualTo("Consultorio");
            assertThat(rCabecera.getCell(7).getStringCellValue()).isEqualTo("Fecha y Hora");
            assertThat(rCabecera.getCell(8).getStringCellValue()).isEqualTo("Turno");
            assertThat(rCabecera.getCell(9).getStringCellValue()).isEqualTo("Estado");

            // Validar datos de la primera cita en fila 4
            Row rDato = sheet.getRow(4);
            assertThat(rDato.getCell(1).getStringCellValue()).isEqualTo("MG-TEST-101");
            assertThat(rDato.getCell(2).getStringCellValue()).isEqualTo("45892134");
            assertThat(rDato.getCell(4).getStringCellValue()).isEqualTo("Cardiología");
            assertThat(rDato.getCell(6).getStringCellValue()).isEqualTo("C-101");
            assertThat(rDato.getCell(9).getStringCellValue()).isEqualTo("Confirmada");

            // Validar fila de resumen
            Row rResumen = sheet.getRow(5);
            assertThat(rResumen.getCell(0).getStringCellValue()).contains("TOTAL REGISTROS EXPORTADOS: 1");
        }
    }

    @Test
    @DisplayName("Apache POI: Generar y verificar reporte de Demanda No Atendida y Afluencia")
    void testExportarReporteDemandaExcel_GeneracionValida() throws IOException {
        DemandaNoAtendida dna = new DemandaNoAtendida();
        Paciente p = new Paciente();
        p.setDni("88997766");
        p.setNombres("Lucia");
        p.setApellidos("Vargas");
        dna.setPaciente(p);

        Especialidad esp = new Especialidad();
        esp.setNombreEspecialidad("Neurología");
        dna.setEspecialidad(esp);

        when(demandaDAO.findAll()).thenReturn(List.of(dna));

        String rutaTest = "target/test-reports/Reporte_Test_Demanda.xlsx";
        File f = reporteService.exportarReporteDemandaExcel(45, rutaTest);

        assertThat(f).exists();
        assertThat(f.length()).isGreaterThan(0);

        try (Workbook wb = WorkbookFactory.create(f)) {
            Sheet sheet = wb.getSheet("Demanda y Afluencia");
            assertThat(sheet).isNotNull();
            Row rSub = sheet.getRow(1);
            assertThat(rSub.getCell(0).getStringCellValue()).contains("Afluencia Actual: 45%");
        }
    }
}
