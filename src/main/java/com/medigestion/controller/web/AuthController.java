package com.medigestion.controller.web;

import com.medigestion.model.Paciente;
import com.medigestion.service.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de autenticación y redirección multi-rol (BPM-1).
 */
@Controller
public class AuthController {

    private final PacienteService pacienteService;

    @Autowired
    public AuthController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping("/")
    public String index(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/default";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            @RequestParam(value = "registered", required = false) String registered,
            Model model
    ) {
        if (error != null) {
            model.addAttribute("errorMsg", "Credenciales incorrectas. Verifique su DNI/Email y contraseña.");
        }
        if (logout != null) {
            model.addAttribute("infoMsg", "Ha cerrado su sesión de forma segura.");
        }
        if (registered != null) {
            model.addAttribute("successMsg", "¡Registro exitoso! Ya puede iniciar sesión con su DNI.");
        }
        return "auth/login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @RequestParam("dni") String dni,
            @RequestParam("nombres") String nombres,
            @RequestParam("apellidos") String apellidos,
            @RequestParam("edad") Short edad,
            @RequestParam("sexo") String sexo,
            @RequestParam("telefono") String telefono,
            @RequestParam("email") String email,
            @RequestParam("contrasena") String contrasena,
            @RequestParam("direccion") String direccion,
            RedirectAttributes redirectAttributes
    ) {
        try {
            pacienteService.registrarPaciente(dni, nombres, apellidos, edad, sexo, telefono, email, contrasena, direccion);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/registro";
        }
    }

    @GetMapping("/default")
    public String redirectPorRol(Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }
        for (GrantedAuthority authority : auth.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_PACIENTE".equals(role)) {
                return "redirect:/paciente/cartelera";
            } else if ("ROLE_MEDICO".equals(role)) {
                return "redirect:/medico/estacion";
            } else if ("ROLE_ADMIN".equals(role)) {
                return "redirect:/admin/dashboard";
            }
        }
        return "redirect:/login";
    }
}
