package com.medigestion.dao;

import com.medigestion.model.ReservaCita;
import com.medigestion.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ReservaCitaDAO extends GenericDAO<ReservaCita, Integer> {

    public ReservaCitaDAO() {
        super(ReservaCita.class);
    }

    public Optional<ReservaCita> findByIdConDetalles(Integer id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            ReservaCita r = em.createQuery(
                    "SELECT r FROM ReservaCita r " +
                    "JOIN FETCH r.paciente " +
                    "JOIN FETCH r.ofertaMedica om " +
                    "JOIN FETCH om.medico " +
                    "JOIN FETCH om.horario " +
                    "JOIN FETCH om.especialidad " +
                    "WHERE r.idReserva = :id",
                    ReservaCita.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(r);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public Optional<ReservaCita> findByCodigoTicket(String codigo) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            ReservaCita r = em.createQuery(
                    "SELECT r FROM ReservaCita r " +
                    "JOIN FETCH r.paciente " +
                    "JOIN FETCH r.ofertaMedica om " +
                    "JOIN FETCH om.medico " +
                    "JOIN FETCH om.horario " +
                    "JOIN FETCH om.especialidad " +
                    "WHERE r.codigoTicket = :codigo",
                    ReservaCita.class)
                    .setParameter("codigo", codigo)
                    .getSingleResult();
            return Optional.of(r);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<ReservaCita> findByPacienteDni(String dni) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM ReservaCita r " +
                    "JOIN FETCH r.ofertaMedica om " +
                    "JOIN FETCH om.medico " +
                    "JOIN FETCH om.horario " +
                    "JOIN FETCH om.especialidad " +
                    "WHERE r.paciente.dni = :dni " +
                    "ORDER BY om.horario.fechaHoraInicio DESC",
                    ReservaCita.class)
                    .setParameter("dni", dni)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<ReservaCita> findCitasActivasDelDia(LocalDateTime inicioDia, LocalDateTime finDia) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT r FROM ReservaCita r " +
                    "JOIN FETCH r.paciente " +
                    "JOIN FETCH r.ofertaMedica om " +
                    "JOIN FETCH om.medico " +
                    "JOIN FETCH om.horario h " +
                    "JOIN FETCH om.especialidad " +
                    "WHERE h.fechaHoraInicio >= :inicio AND h.fechaHoraInicio <= :fin " +
                    "AND r.estado IN ('Pendiente', 'Confirmada') " +
                    "ORDER BY h.fechaHoraInicio ASC",
                    ReservaCita.class)
                    .setParameter("inicio", inicioDia)
                    .setParameter("fin", finDia)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public long countCitasActivasSemanaPorEspecialidad(String dni, Integer idEspecialidad, LocalDateTime inicioSemana, LocalDateTime finSemana) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT COUNT(r) FROM ReservaCita r " +
                    "WHERE r.paciente.dni = :dni " +
                    "AND r.ofertaMedica.especialidad.idEspecialidad = :espId " +
                    "AND r.ofertaMedica.horario.fechaHoraInicio >= :inicio " +
                    "AND r.ofertaMedica.horario.fechaHoraInicio <= :fin " +
                    "AND r.estado NOT IN ('Cancelada', 'Reprogramada')",
                    Long.class)
                    .setParameter("dni", dni)
                    .setParameter("espId", idEspecialidad)
                    .setParameter("inicio", inicioSemana)
                    .setParameter("fin", finSemana)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}
