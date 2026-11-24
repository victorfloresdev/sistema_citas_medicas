package com.medigestion.controller.web;

import com.medigestion.dao.ReservaCitaDAO;
import com.medigestion.model.ReservaCita;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador público para la Pantalla TV de la Sala de Espera (BPM-3 / Hito 60%).
 * Permite a los pacientes en sala visualizar los llamados en tiempo real hacia los consultorios.
 */
@Controller
@RequestMapping("/sala-espera")
public class SalaEsperaWebController {

    private final ReservaCitaDAO reservaCitaDAO;

    @Autowired
    public SalaEsperaWebController(ReservaCitaDAO reservaCitaDAO) {
        this.reservaCitaDAO = reservaCitaDAO;
    }

    @GetMapping
    public String pantalla(Model model) {
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        LocalDateTime finHoy = LocalDate.now().atTime(23, 59, 59);

        List<ReservaCita> llamados = reservaCitaDAO.findUltimasCitasLlamadas(inicioHoy, finHoy);

        ReservaCita turnoActual = !llamados.isEmpty() ? llamados.get(0) : null;
        List<ReservaCita> historialLlamados = llamados.size() > 1 ? llamados.subList(1, Math.min(llamados.size(), 6)) : List.of();

        model.addAttribute("turnoActual", turnoActual);
        model.addAttribute("historialLlamados", historialLlamados);
        model.addAttribute("totalLlamados", llamados.size());

        return "medico/sala-espera";
    }
}
