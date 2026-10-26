package com.medigestion.dao;

import com.medigestion.model.ReservaCita;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrato específico para operaciones de persistencia de Reservas de Cita (ISP, DIP).
 */
public interface IReservaCitaDAO extends IDAO<ReservaCita, Integer> {
    Optional<ReservaCita> findByIdConDetalles(Integer id);
    Optional<ReservaCita> findByCodigoTicket(String codigo);
    List<ReservaCita> findByPacienteDni(String dni);
    List<ReservaCita> findCitasActivasDelDia(LocalDateTime inicioDia, LocalDateTime finDia);
    long countCitasActivasSemanaPorEspecialidad(String dni, Integer idEspecialidad, LocalDateTime inicioSemana, LocalDateTime finSemana);
    public List<ReservaCita> findAllConDetallesCompletos();
}
