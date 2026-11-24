package com.medigestion.repository;

import com.medigestion.model.ReservaCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservaCitaRepository extends JpaRepository<ReservaCita, Integer> {

    @Query("SELECT r FROM ReservaCita r JOIN FETCH r.paciente JOIN FETCH r.ofertaMedica om JOIN FETCH om.medico JOIN FETCH om.horario JOIN FETCH om.especialidad WHERE r.idReserva = :id")
    Optional<ReservaCita> findByIdConDetalles(@Param("id") Integer id);

    @Query("SELECT r FROM ReservaCita r JOIN FETCH r.paciente JOIN FETCH r.ofertaMedica om JOIN FETCH om.medico JOIN FETCH om.horario JOIN FETCH om.especialidad WHERE r.paciente.dni = :dni ORDER BY r.fechaCreacion DESC")
    List<ReservaCita> findByPacienteDniConDetalles(@Param("dni") String dni);

    @Query("SELECT r FROM ReservaCita r JOIN FETCH r.paciente JOIN FETCH r.ofertaMedica om JOIN FETCH om.medico JOIN FETCH om.horario JOIN FETCH om.especialidad WHERE om.medico.idMedico = :idMedico AND om.horario.fechaHoraInicio >= :inicio AND om.horario.fechaHoraInicio <= :fin ORDER BY om.horario.fechaHoraInicio ASC")
    List<ReservaCita> findCitasDelDiaPorMedico(@Param("idMedico") Integer idMedico, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(r) FROM ReservaCita r WHERE r.paciente.dni = :dni AND r.ofertaMedica.especialidad.idEspecialidad = :idEspecialidad AND r.estado = 'Confirmada' AND r.ofertaMedica.horario.fechaHoraInicio >= :inicioSemana AND r.ofertaMedica.horario.fechaHoraInicio <= :finSemana")
    long countCitasActivasSemanaPorEspecialidad(@Param("dni") String dni, @Param("idEspecialidad") Integer idEspecialidad, @Param("inicioSemana") LocalDateTime inicioSemana, @Param("finSemana") LocalDateTime finSemana);

    @Query("SELECT r FROM ReservaCita r JOIN FETCH r.paciente JOIN FETCH r.ofertaMedica om JOIN FETCH om.medico JOIN FETCH om.horario JOIN FETCH om.especialidad WHERE r.estado = 'Confirmada' AND r.fechaLlamado IS NOT NULL AND om.horario.fechaHoraInicio >= :inicio AND om.horario.fechaHoraInicio <= :fin ORDER BY r.fechaLlamado DESC")
    List<ReservaCita> findUltimasCitasLlamadas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
