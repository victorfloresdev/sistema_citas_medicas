package com.medigestion.service;

import com.medigestion.dao.PacienteDAO;
import com.medigestion.dao.BitacoraEventoDAO;
import com.medigestion.model.Paciente;
import com.medigestion.model.BitacoraEvento;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

/**
 * Servicio para gestión de pacientes y autenticación (RF-01).
 */
public class PacienteService {

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final BitacoraEventoDAO bitacoraDAO = new BitacoraEventoDAO();

    /**
     * Valida la identidad del asegurado mediante DNI.
     * Si no existe, devuelve Optional.empty() para que el flujo active el registro ágil.
     */
    public Optional<Paciente> identificarPaciente(String dni) {
        if (dni == null || !dni.matches("^[0-9]{8}$")) {
            throw new IllegalArgumentException("DNI inválido. Debe tener exactamente 8 dígitos.");
        }
        return pacienteDAO.findByDni(dni);
    }

    /**
     * Registra un nuevo paciente con datos básicos de manera ágil.
     */
    public Paciente registrarPaciente(String dni, String nombres, String apellidos, 
                                     short edad, String sexo, String telefono, 
                                     String email, String passwordPlana, String direccion) {
        // Validaciones
        if (pacienteDAO.findByDni(dni).isPresent()) {
            throw new IllegalStateException("El paciente con DNI " + dni + " ya se encuentra registrado.");
        }
        if (edad < 0 || edad > 120) {
            throw new IllegalArgumentException("Edad fuera de rango permitido (0-120).");
        }
        if (!sexo.equalsIgnoreCase("HOMBRE") && !sexo.equalsIgnoreCase("MUJER")) {
            throw new IllegalArgumentException("Sexo debe ser HOMBRE o MUJER.");
        }

        // Hashing seguro de contraseña con BCrypt (RNF-04)
        String hash = BCrypt.hashpw(passwordPlana, BCrypt.gensalt());

        Paciente p = new Paciente();
        p.setDni(dni);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setEdad(edad);
        p.setSexo(sexo.toUpperCase());
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setContrasenaHash(hash);
        p.setDireccion(direccion);

        Paciente creado = pacienteDAO.save(p);

        bitacoraDAO.save(new BitacoraEvento("PacienteService", "INFO", 
                "Nuevo paciente registrado con éxito: DNI " + dni + " (" + nombres + " " + apellidos + ")"));

        return creado;
    }
}
