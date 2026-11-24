package com.medigestion.controller.web;

import com.medigestion.dao.MedicoDAO;
import com.medigestion.dao.ReservaCitaDAO;
import com.medigestion.model.Medico;
import com.medigestion.model.ReservaCita;
import com.medigestion.service.AtencionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la Estación Médica (BPM-3 / Hito 60%).
 * Permite a los médicos visualizar su agenda del día, llamar pacientes a consultorio y finalizar consultas.
 */
@Controller
@RequestMapping("/medico")
public class MedicoWebController {

    private final MedicoDAO medicoDAO;
    private final ReservaCitaDAO reservaCitaDAO;
    private final AtencionService atencionService;

    @Autowired
    public MedicoWebController(MedicoDAO medicoDAO, ReservaCitaDAO reservaCitaDAO, AtencionService atencionService) {
        this.medicoDAO = medicoDAO;
        this.reservaCitaDAO = reservaCitaDAO;
        this.atencionService = atencionService;
    }

    private Medico obtenerMedicoAutenticado(Authentication auth) {
        String email = auth.getName();
        return medicoDAO.findAll().stream()
                .filter(m -> email.equalsIgnoreCase(m.getEmail()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Médico no encontrado para la sesión"));
    }

    @GetMapping("/estacion")
    public String estacion(Authentication auth, Model model) {
        Medico medico = obtenerMedicoAutenticado(auth);
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy = LocalDate.now().atTime(23, 59, 59);

        List<ReservaCita> citas = reservaCitaDAO.findCitasDelDiaPorMedico(medico.getIdMedico(), inicioHoy, finHoy);

        model.addAttribute("medico", medico);
        model.addAttribute("citas", citas);
        return "medico/estacion";
    }

    @PostMapping("/llamar")
    public String llamarPaciente(
            @RequestParam("idReserva") Integer idReserva,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ReservaCita cita = atencionService.llamarPaciente(idReserva);
            redirectAttributes.addFlashAttribute("successMsg", 
                    "Paciente " + cita.getPaciente().getNombreCompleto() + " llamado al consultorio.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/medico/estacion";
    }

    @PostMapping("/atender")
    public String atenderPaciente(
            @RequestParam("idReserva") Integer idReserva,
            RedirectAttributes redirectAttributes
    ) {
        try {
            atencionService.marcarCompletada(idReserva);
            redirectAttributes.addFlashAttribute("successMsg", "Consulta finalizada con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/medico/estacion";
    }
}
