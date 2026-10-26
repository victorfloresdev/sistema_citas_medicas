package com.medigestion.dao;

import com.medigestion.model.OfertaMedica;
import java.util.List;
import java.util.Optional;

/**
 * Contrato específico para operaciones de persistencia de Oferta Médica y Horarios (ISP, DIP).
 */
public interface IOfertaMedicaDAO extends IDAO<OfertaMedica, Integer> {
    Optional<OfertaMedica> findByIdConDetalles(Integer id);
    List<OfertaMedica> findDisponiblesByEspecialidad(Integer idEspecialidad);
    List<OfertaMedica> findDisponiblesByEspecialidadYMedico(Integer idEspecialidad, Integer idMedico);
    List<OfertaMedica> findAllConDetalles();
}
