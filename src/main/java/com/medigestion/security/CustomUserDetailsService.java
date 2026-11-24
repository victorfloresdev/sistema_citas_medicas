package com.medigestion.security;

import com.medigestion.dao.AdministradorDAO;
import com.medigestion.dao.MedicoDAO;
import com.medigestion.dao.PacienteDAO;
import com.medigestion.model.Administrador;
import com.medigestion.model.Medico;
import com.medigestion.model.Paciente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * Servicio de autenticación multi-rol unificado para Spring Security 6 (RBAC).
 * Permite inicio de sesión transparente para:
 * - Pacientes: por DNI
 * - Médicos: por Email
 * - Administradores: por Email
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PacienteDAO pacienteDAO;
    private final MedicoDAO medicoDAO;
    private final AdministradorDAO adminDAO;

    @Autowired
    public CustomUserDetailsService(PacienteDAO pacienteDAO, MedicoDAO medicoDAO, AdministradorDAO adminDAO) {
        this.pacienteDAO = pacienteDAO;
        this.medicoDAO = medicoDAO;
        this.adminDAO = adminDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String cleanUsername = username != null ? username.trim() : "";

        // 1. Intentar como Paciente (por DNI)
        if (cleanUsername.matches("^[0-9]{8}$")) {
            Optional<Paciente> p = pacienteDAO.findByDni(cleanUsername);
            if (p.isPresent()) {
                Paciente pac = p.get();
                return new User(
                        pac.getDni(),
                        pac.getContrasenaHash(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_PACIENTE"))
                );
            }
        }

        // 2. Intentar como Administrador (por Email)
        Optional<Administrador> admin = adminDAO.findByEmail(cleanUsername);
        if (admin.isPresent() && Boolean.TRUE.equals(admin.get().getActivo())) {
            Administrador adm = admin.get();
            return new User(
                    adm.getEmail(),
                    adm.getContrasenaHash(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        // 3. Intentar como Médico (por Email)
        Optional<Medico> medico = medicoDAO.findAll().stream()
                .filter(m -> cleanUsername.equalsIgnoreCase(m.getEmail()) && Boolean.TRUE.equals(m.getActivo()))
                .findFirst();

        if (medico.isPresent()) {
            Medico med = medico.get();
            return new User(
                    med.getEmail(),
                    med.getContrasenaHash(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEDICO"))
            );
        }

        throw new UsernameNotFoundException("Usuario no encontrado con identificador: " + cleanUsername);
    }
}
