package com.medigestion.repository;

import com.medigestion.model.OfertaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfertaMedicaRepository extends JpaRepository<OfertaMedica, Integer> {

    @Query("SELECT o FROM OfertaMedica o JOIN FETCH o.medico JOIN FETCH o.horario JOIN FETCH o.especialidad WHERE o.idOferta = :id")
    Optional<OfertaMedica> findByIdConDetalles(@Param("id") Integer id);

    @Query("SELECT o FROM OfertaMedica o JOIN FETCH o.medico JOIN FETCH o.horario JOIN FETCH o.especialidad WHERE o.especialidad.idEspecialidad = :idEspecialidad AND o.estado = 'Disponible' ORDER BY o.horario.fechaHoraInicio ASC")
    List<OfertaMedica> findDisponiblesByEspecialidad(@Param("idEspecialidad") Integer idEspecialidad);

    @Query("SELECT o FROM OfertaMedica o JOIN FETCH o.medico JOIN FETCH o.horario JOIN FETCH o.especialidad ORDER BY o.horario.fechaHoraInicio ASC")
    List<OfertaMedica> findAllConDetalles();
}
