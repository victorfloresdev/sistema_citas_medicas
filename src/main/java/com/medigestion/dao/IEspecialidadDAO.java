package com.medigestion.dao;

import com.medigestion.model.Especialidad;
import java.util.Optional;

/**
 * Contrato específico para operaciones de persistencia de Especialidades Médicas (ISP, DIP).
 */
public interface IEspecialidadDAO extends IDAO<Especialidad, Integer> {
    Optional<Especialidad> findByNombre(String nombre);
}
