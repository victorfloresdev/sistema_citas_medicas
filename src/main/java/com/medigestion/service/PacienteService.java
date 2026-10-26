package com.medigestion.service;

import com.google.common.base.Preconditions;
import com.medigestion.dao.BitacoraEventoDAO;
import com.medigestion.dao.IBitacoraEventoDAO;
import com.medigestion.dao.IPacienteDAO;
import com.medigestion.dao.PacienteDAO;
import com.medigestion.model.BitacoraEvento;
import com.medigestion.model.Paciente;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Servicio para gestión de pacientes y validación de identidad (RF-01, RNF-04).
 * Aplica principios SOLID (SRP, DIP) e incorpora Google Guava, Apache Commons y Logback.
 */
public class PacienteService {

    private static final Logger log = LoggerFactory.getLogger(PacienteService.class);

    private final IPacienteDAO pacienteDAO;
    private final IBitacoraEventoDAO bitacoraDAO;

    public PacienteService() {
        this(new PacienteDAO(), new BitacoraEventoDAO());
    }

    public PacienteService(IPacienteDAO pacienteDAO, IBitacoraEventoDAO bitacoraDAO) {
        this.pacienteDAO = Preconditions.checkNotNull(pacienteDAO, "pacienteDAO no puede ser nulo");
        this.bitacoraDAO = Preconditions.checkNotNull(bitacoraDAO, "bitacoraDAO no puede ser nulo");
    }

    /**
     * Valida la identidad del asegurado mediante DNI de 8 dígitos numéricos (RF-01).
     * Si no existe, devuelve Optional.empty() para que el flujo active el registro ágil.
     */
    public Optional<Paciente> identificarPaciente(String dni) {
        Preconditions.checkArgument(StringUtils.isNotBlank(dni), "El DNI no puede estar vacío.");
        String dniLimpio = StringUtils.trim(dni);
        Preconditions.checkArgument(dniLimpio.matches("^[0-9]{8}$"), "DNI inválido. Debe contener exactamente 8 dígitos numéricos.");

        log.info("[RF-01 Identidad] Validando DNI: {}", dniLimpio);
        Optional<Paciente> paciente = pacienteDAO.findByDni(dniLimpio);
        if (paciente.isPresent()) {
            log.info("[RF-01 Identidad] Paciente encontrado: {} {}", paciente.get().getNombres(), paciente.get().getApellidos());
        } else {
            log.info("[RF-01 Identidad] DNI {} no registrado. Habilitando registro ágil.", dniLimpio);
        }
        return paciente;
    }

    /**
     * Registra un nuevo paciente con validaciones de seguridad e integridad (RNF-04, RNF-05).
     * Utiliza Apache Commons Validator para el correo y BCrypt para el cifrado seguro de la credencial.
     */
    public Paciente registrarPaciente(String dni, String nombres, String apellidos, 
                                     short edad, String sexo, String telefono, 
                                     String email, String passwordPlana, String direccion) {
        Preconditions.checkArgument(StringUtils.isNotBlank(dni) && dni.matches("^[0-9]{8}$"),
                "El DNI debe contener exactamente 8 dígitos numéricos.");
        Preconditions.checkArgument(StringUtils.isNotBlank(nombres), "Los nombres son obligatorios.");
        Preconditions.checkArgument(StringUtils.isNotBlank(apellidos), "Los apellidos son obligatorios.");
        Preconditions.checkArgument(edad >= 0 && edad <= 120, "Edad fuera de rango permitido (0-120).");
        
        String sexoUpper = StringUtils.upperCase(StringUtils.trimToEmpty(sexo));
        Preconditions.checkArgument(sexoUpper.equals("HOMBRE") || sexoUpper.equals("MUJER"),
                "Sexo debe ser HOMBRE o MUJER.");

        if (StringUtils.isNotBlank(email)) {
            Preconditions.checkArgument(EmailValidator.getInstance().isValid(email.trim()),
                    "El formato del correo electrónico es inválido.");
        }

        Preconditions.checkArgument(StringUtils.isNotBlank(passwordPlana) && passwordPlana.length() >= 4,
                "La contraseña debe contener al menos 4 caracteres.");

        // Verificar duplicidad
        if (pacienteDAO.findByDni(dni).isPresent()) {
            String msg = "El paciente con DNI " + dni + " ya se encuentra registrado.";
            log.warn("[RF-01 Registro] {}", msg);
            throw new IllegalStateException(msg);
        }

        // Hashing seguro de contraseña con BCrypt (RNF-04)
        String hash = BCrypt.hashpw(passwordPlana, BCrypt.gensalt(12));

        Paciente p = new Paciente();
        p.setDni(dni.trim());
        p.setNombres(StringUtils.capitalize(StringUtils.trim(nombres)));
        p.setApellidos(StringUtils.capitalize(StringUtils.trim(apellidos)));
        p.setEdad(edad);
        p.setSexo(sexoUpper);
        p.setTelefono(StringUtils.trimToNull(telefono));
        p.setEmail(StringUtils.trimToNull(email));
        p.setContrasenaHash(hash);
        p.setDireccion(StringUtils.trimToNull(direccion));

        Paciente creado = pacienteDAO.save(p);

        String auditoria = String.format("Nuevo paciente registrado con éxito: DNI %s (%s %s)",
                dni, creado.getNombres(), creado.getApellidos());
        log.info("[RF-01 Registro Exitoso] {}", auditoria);
        bitacoraDAO.save(new BitacoraEvento("PacienteService", "INFO", auditoria));

        return creado;
    }

    /**
     * Autentica a un paciente verificando su hash de contraseña con BCrypt.
     */
    public boolean verificarContrasena(Paciente paciente, String passwordPlana) {
        if (paciente == null || StringUtils.isBlank(passwordPlana) || StringUtils.isBlank(paciente.getContrasenaHash())) {
            return false;
        }
        return BCrypt.checkpw(passwordPlana, paciente.getContrasenaHash());
    }
}
