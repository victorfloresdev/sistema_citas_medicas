package com.medigestion.service;

import com.google.common.base.Preconditions;
import com.medigestion.dao.DemandaNoAtendidaDAO;
import com.medigestion.dao.IDemandaNoAtendidaDAO;
import com.medigestion.dao.IReservaCitaDAO;
import com.medigestion.dao.ReservaCitaDAO;
import com.medigestion.model.DemandaNoAtendida;
import com.medigestion.model.ReservaCita;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio para generación y exportación de reportes institucionales en formato Excel (.xlsx)
 * utilizando Apache POI (REP-01, R01, R02, R03).
 * Cumple con la rúbrica de recursos Java y principios SOLID (SRP, DIP).
 */
public class ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteService.class);
    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IReservaCitaDAO reservaCitaDAO;
    private final IDemandaNoAtendidaDAO demandaDAO;

    public ReporteService() {
        this(new ReservaCitaDAO(), new DemandaNoAtendidaDAO());
    }

    public ReporteService(IReservaCitaDAO reservaCitaDAO, IDemandaNoAtendidaDAO demandaDAO) {
        this.reservaCitaDAO = Preconditions.checkNotNull(reservaCitaDAO, "reservaCitaDAO no puede ser nulo");
        this.demandaDAO = Preconditions.checkNotNull(demandaDAO, "demandaDAO no puede ser nulo");
    }

    /**
     * Genera el reporte oficial de citas médicas programadas e históricas en un archivo Excel .xlsx (REP-01).
     *
     * @param rutaDestino Ruta donde se guardará el archivo Excel (ej: "reportes/Reporte_Citas_Medicas.xlsx").
     * @return El archivo File generado y cerrado correctamente.
     * @throws IOException Si ocurre algún error en la escritura en disco.
     */
    public File exportarReporteCitasExcel(String rutaDestino) throws IOException {
        List<ReservaCita> citas = reservaCitaDAO.findAll();
        return exportarReporteCitasExcel(citas, rutaDestino);
    }

    /**
     * Sobrecarga desacoplada para exportar una lista específica de citas médicas (apoyo a TDD).
     */
    public File exportarReporteCitasExcel(List<ReservaCita> citas, String rutaDestino) throws IOException {
        Preconditions.checkNotNull(citas, "La lista de citas no puede ser nula.");
        Preconditions.checkArgument(StringUtils.isNotBlank(rutaDestino), "La ruta de destino es obligatoria.");

        File archivo = new File(rutaDestino);
        File directorioPadre = archivo.getParentFile();
        if (directorioPadre != null && !directorioPadre.exists()) {
            directorioPadre.mkdirs();
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Citas Médicas");

            // Estilos
            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloSubtitulo = crearEstiloSubtitulo(workbook);
            CellStyle estiloCabecera = crearEstiloCabecera(workbook);
            CellStyle estiloDatoNormal = crearEstiloDato(workbook, false, HorizontalAlignment.LEFT);
            CellStyle estiloDatoCentrado = crearEstiloDato(workbook, false, HorizontalAlignment.CENTER);
            CellStyle estiloDatoZebra = crearEstiloDato(workbook, true, HorizontalAlignment.LEFT);
            CellStyle estiloDatoZebraCentrado = crearEstiloDato(workbook, true, HorizontalAlignment.CENTER);
            CellStyle estiloResumen = crearEstiloResumen(workbook);

            int filaActual = 0;

            // 1. Título institucional
            Row rowTitulo = sheet.createRow(filaActual++);
            Cell celdaTitulo = rowTitulo.createCell(0);
            celdaTitulo.setCellValue("MEDIGESTIÓN - PORTAL INTEGRADO DE CITAS MÉDICAS");
            celdaTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            // 2. Subtítulo con metadatos
            Row rowSubtitulo = sheet.createRow(filaActual++);
            Cell celdaSub = rowSubtitulo.createCell(0);
            celdaSub.setCellValue("Reporte Oficial de Citas Programadas · Generado: " +
                    LocalDateTime.now().format(FMT_FECHA_HORA) + " · Alcance: 40% (BPM-1)");
            celdaSub.setCellStyle(estiloSubtitulo);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 9));

            filaActual++; // Espacio en blanco

            // 3. Encabezados de tabla
            String[] cabeceras = {
                    "N°", "Código Ticket", "DNI Asegurado", "Paciente",
                    "Especialidad", "Médico Tratante", "Consultorio", "Fecha y Hora", "Turno", "Estado"
            };

            Row rowCabecera = sheet.createRow(filaActual++);
            rowCabecera.setHeightInPoints(24);
            for (int i = 0; i < cabeceras.length; i++) {
                Cell celda = rowCabecera.createCell(i);
                celda.setCellValue(cabeceras[i]);
                celda.setCellStyle(estiloCabecera);
            }

            // 4. Llenado de filas de datos
            int indice = 1;
            for (ReservaCita c : citas) {
                Row row = sheet.createRow(filaActual++);
                boolean esPar = (indice % 2 == 0);
                CellStyle estiloTexto = esPar ? estiloDatoZebra : estiloDatoNormal;
                CellStyle estiloCentrado = esPar ? estiloDatoZebraCentrado : estiloDatoCentrado;

                Cell c0 = row.createCell(0);
                c0.setCellValue(indice++);
                c0.setCellStyle(estiloCentrado);

                Cell c1 = row.createCell(1);
                c1.setCellValue(c.getCodigoTicket() != null ? c.getCodigoTicket() : "—");
                c1.setCellStyle(estiloCentrado);

                Cell c2 = row.createCell(2);
                c2.setCellValue(c.getPaciente() != null ? c.getPaciente().getDni() : "—");
                c2.setCellStyle(estiloCentrado);

                Cell c3 = row.createCell(3);
                c3.setCellValue(c.getPaciente() != null ? c.getPaciente().getNombreCompleto() : "—");
                c3.setCellStyle(estiloTexto);

                Cell c4 = row.createCell(4);
                c4.setCellValue(c.getOfertaMedica() != null && c.getOfertaMedica().getEspecialidad() != null ?
                        c.getOfertaMedica().getEspecialidad().getNombreEspecialidad() : "—");
                c4.setCellStyle(estiloTexto);

                Cell c5 = row.createCell(5);
                c5.setCellValue(c.getOfertaMedica() != null && c.getOfertaMedica().getMedico() != null ?
                        c.getOfertaMedica().getMedico().getNombreCompleto() : "—");
                c5.setCellStyle(estiloTexto);

                Cell c6 = row.createCell(6);
                c6.setCellValue(c.getOfertaMedica() != null && c.getOfertaMedica().getMedico() != null ?
                        c.getOfertaMedica().getMedico().getConsultorio() : "—");
                c6.setCellStyle(estiloCentrado);

                Cell c7 = row.createCell(7);
                String strFecha = (c.getOfertaMedica() != null && c.getOfertaMedica().getHorario() != null &&
                        c.getOfertaMedica().getHorario().getFechaHoraInicio() != null) ?
                        c.getOfertaMedica().getHorario().getFechaHoraInicio().format(FMT_FECHA_HORA) : "—";
                c7.setCellValue(strFecha);
                c7.setCellStyle(estiloCentrado);

                Cell c8 = row.createCell(8);
                c8.setCellValue(c.getTurno() != null ? c.getTurno() : "—");
                c8.setCellStyle(estiloCentrado);

                Cell c9 = row.createCell(9);
                c9.setCellValue(c.getEstado() != null ? c.getEstado() : "—");
                c9.setCellStyle(estiloCentrado);
            }

            // 5. Fila de resumen inferior
            Row rowResumen = sheet.createRow(filaActual);
            Cell celdaResumenLabel = rowResumen.createCell(0);
            celdaResumenLabel.setCellValue("TOTAL REGISTROS EXPORTADOS: " + citas.size());
            celdaResumenLabel.setCellStyle(estiloResumen);
            sheet.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, 9));

            // Auto-ajustar ancho de columnas
            for (int i = 0; i < cabeceras.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1200, 3000));
            }

            // Escribir a disco
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                workbook.write(fos);
            }

            log.info("[REP-01 Apache POI] Reporte de Citas generado exitosamente: {} (Total filas: {})",
                    archivo.getAbsolutePath(), citas.size());
            return archivo;
        }
    }

    /**
     * Genera el reporte de demanda insatisfecha y afluencia en Excel (RF-07, R02, R03).
     */
    public File exportarReporteDemandaExcel(int afluenciaActual, String rutaDestino) throws IOException {
        List<DemandaNoAtendida> demandas = demandaDAO.findAll();
        Preconditions.checkArgument(StringUtils.isNotBlank(rutaDestino), "La ruta de destino es obligatoria.");

        File archivo = new File(rutaDestino);
        File dir = archivo.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Demanda y Afluencia");

            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloSubtitulo = crearEstiloSubtitulo(workbook);
            CellStyle estiloCabecera = crearEstiloCabecera(workbook);
            CellStyle estiloDatoNormal = crearEstiloDato(workbook, false, HorizontalAlignment.LEFT);
            CellStyle estiloDatoCentrado = crearEstiloDato(workbook, false, HorizontalAlignment.CENTER);

            int fila = 0;
            Row r0 = sheet.createRow(fila++);
            Cell cTitulo = r0.createCell(0);
            cTitulo.setCellValue("MEDIGESTIÓN - REPORTE DE DEMANDA NO ATENDIDA Y AFLUENCIA (RF-07)");
            cTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));

            Row r1 = sheet.createRow(fila++);
            Cell cSub = r1.createCell(0);
            cSub.setCellValue("Indicador de Afluencia Actual: " + afluenciaActual + "% | Fecha: " +
                    LocalDateTime.now().format(FMT_FECHA_HORA));
            cSub.setCellStyle(estiloSubtitulo);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 4));

            fila++; // espacio

            String[] headers = {"ID", "DNI Paciente", "Nombre Paciente", "Especialidad Buscada", "Fecha Solicitud"};
            Row rCabecera = sheet.createRow(fila++);
            for (int i = 0; i < headers.length; i++) {
                Cell c = rCabecera.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(estiloCabecera);
            }

            int idx = 1;
            for (DemandaNoAtendida d : demandas) {
                Row r = sheet.createRow(fila++);
                Cell c0 = r.createCell(0);
                c0.setCellValue(idx++);
                c0.setCellStyle(estiloDatoCentrado);

                Cell c1 = r.createCell(1);
                c1.setCellValue(d.getPaciente() != null ? d.getPaciente().getDni() : "—");
                c1.setCellStyle(estiloDatoCentrado);

                Cell c2 = r.createCell(2);
                c2.setCellValue(d.getPaciente() != null ? d.getPaciente().getNombreCompleto() : "—");
                c2.setCellStyle(estiloDatoNormal);

                Cell c3 = r.createCell(3);
                c3.setCellValue(d.getEspecialidad() != null ? d.getEspecialidad().getNombreEspecialidad() : "—");
                c3.setCellStyle(estiloDatoNormal);

                Cell c4 = r.createCell(4);
                c4.setCellValue(d.getFechaRegistro() != null ? d.getFechaRegistro().format(FMT_FECHA_HORA) : "—");
                c4.setCellStyle(estiloDatoCentrado);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i) + 1000, 3200));
            }

            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                workbook.write(fos);
            }

            log.info("[REP-01 Apache POI] Reporte de Demanda generado exitosamente: {}", archivo.getAbsolutePath());
            return archivo;
        }
    }

    // --- Métodos de apoyo de diseño y estilos Apache POI ---

    private CellStyle crearEstiloTitulo(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloSubtitulo(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle crearEstiloCabecera(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBordes(style);
        return style;
    }

    private CellStyle crearEstiloDato(Workbook wb, boolean esZebra, HorizontalAlignment align) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(align);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        if (esZebra) {
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        setBordes(style);
        return style;
    }

    private CellStyle crearEstiloResumen(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        setBordes(style);
        return style;
    }

    private void setBordes(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
