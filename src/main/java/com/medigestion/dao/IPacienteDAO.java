package com.medigestion.dao;

import com.medigestion.model.Paciente;
import java.util.Optional;

/**
 * Contrato específico para operaciones de persistencia de Pacientes (ISP, DIP).
 */
public interface IPacienteDAO extends IDAO<Paciente, String> {
    Optional<Paciente> findByDni(String dni);
    Optional<Paciente> findByEmail(String email);
}
