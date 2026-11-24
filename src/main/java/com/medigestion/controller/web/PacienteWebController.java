package com.medigestion.controller.web;

import com.medigestion.dao.EspecialidadDAO;
import com.medigestion.dao.OfertaMedicaDAO;
import com.medigestion.dao.ReservaCitaDAO;
import com.medigestion.model.Especialidad;
import com.medigestion.model.OfertaMedica;
import com.medigestion.model.Paciente;
import com.medigestion.model.ReservaCita;
import com.medigestion.service.CitaService;
import com.medigestion.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controlador web para el Portal del Paciente (BPM-1 y BPM-2).
 * Gestiona cartelera, selección de cupo, regla RF-04, ticket digital y cancelación con regla de 60 min (RF-06).
 */
@Controller
@RequestMapping("/paciente")
public class PacienteWebController {

    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final EspecialidadDAO especialidadDAO;
    private final OfertaMedicaDAO ofertaMedicaDAO;
    private final ReservaCitaDAO reservaCitaDAO;

    @Autowired
    public PacienteWebController(
            CitaService citaService,
            PacienteService pacienteService,
            EspecialidadDAO especialidadDAO,
            OfertaMedicaDAO ofertaMedicaDAO,
            ReservaCitaDAO reservaCitaDAO
    ) {
        this.citaService = citaService;
        this.pacienteService = pacienteService;
        this.especialidadDAO = especialidadDAO;
        this.ofertaMedicaDAO = ofertaMedicaDAO;
        this.reservaCitaDAO = reservaCitaDAO;
    }

    private Paciente obtenerPacienteAutenticado(Authentication auth) {
        String dni = auth.getName();
        return pacienteService.identificarPaciente(dni)
                .orElseThrow(() -> new IllegalStateException("Paciente no encontrado para sesión activa"));
    }

    @GetMapping("/cartelera")
    public String cartelera(
            @RequestParam(value = "especialidadId", required = false) Integer especialidadId,
            Authentication auth,
            Model model
    ) {
        Paciente paciente = obtenerPacienteAutenticado(auth);
        List<Especialidad> especialidades = especialidadDAO.findAll();
        List<OfertaMedica> ofertas;

        if (especialidadId != null && especialidadId > 0) {
            ofertas = citaService.buscarOfertasDisponibles(especialidadId, paciente);
        } else if (!especialidades.isEmpty()) {
            especialidadId = especialidades.get(0).getIdEspecialidad();
            ofertas = citaService.buscarOfertasDisponibles(especialidadId, paciente);
        } else {
            ofertas = Collections.emptyList();
        }

        int afluencia = citaService.calcularAfluenciaActual();

        model.addAttribute("paciente", paciente);
        model.addAttribute("especialidades", especialidades);
        model.addAttribute("ofertas", ofertas);
        model.addAttribute("especialidadSeleccionada", especialidadId);
        model.addAttribute("afluenciaActual", afluencia);

        return "paciente/cartelera";
    }

    @GetMapping("/confirmar/{idOferta}")
    public String confirmar(
            @PathVariable("idOferta") Integer idOferta,
            Authentication auth,
            Model model
    ) {
        Paciente paciente = obtenerPacienteAutenticado(auth);
        Optional<OfertaMedica> ofertaOpt = ofertaMedicaDAO.findByIdConDetalles(idOferta);
        if (ofertaOpt.isEmpty()) {
            return "redirect:/paciente/cartelera";
        }

        model.addAttribute("paciente", paciente);
        model.addAttribute("oferta", ofertaOpt.get());
        return "paciente/confirmar-reserva";
    }

    @PostMapping("/reservar")
    public String reservar(
            @RequestParam("idOferta") Integer idOferta,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Paciente paciente = obtenerPacienteAutenticado(auth);
            ReservaCita cita = citaService.reservarCita(paciente, idOferta);
            redirectAttributes.addFlashAttribute("successMsg", 
                    "¡Cita confirmada con éxito! Su ticket de atención es: " + cita.getCodigoTicket());
            return "redirect:/paciente/mis-citas";
        } catch (IllegalStateException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/paciente/cartelera";
        }
    }

    @GetMapping("/mis-citas")
    public String misCitas(Authentication auth, Model model) {
        Paciente paciente = obtenerPacienteAutenticado(auth);
        List<ReservaCita> citas = reservaCitaDAO.findByPacienteDniConDetalles(paciente.getDni());

        model.addAttribute("paciente", paciente);
        model.addAttribute("citas", citas);
        return "paciente/mis-citas";
    }

    @PostMapping("/cancelar")
    public String cancelar(
            @RequestParam("idReserva") Integer idReserva,
            @RequestParam("motivo") String motivo,
            Authentication auth,
            RedirectAttributes redirectAttributes
    ) {
        try {
            citaService.cancelarCita(idReserva, "WEB", motivo);
            redirectAttributes.addFlashAttribute("successMsg", "La cita ha sido cancelada y el cupo médico fue liberado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/paciente/mis-citas";
    }
}
