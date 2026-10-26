package com.medigestion.dao;

import com.medigestion.model.OfertaMedica;
import com.medigestion.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class OfertaMedicaDAO extends GenericDAO<OfertaMedica, Integer> implements IOfertaMedicaDAO {

    public OfertaMedicaDAO() {
        super(OfertaMedica.class);
    }

    public Optional<OfertaMedica> findByIdConDetalles(Integer id) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            OfertaMedica o = em.createQuery(
                    "SELECT o FROM OfertaMedica o " +
                    "JOIN FETCH o.medico " +
                    "JOIN FETCH o.horario " +
                    "JOIN FETCH o.especialidad " +
                    "JOIN FETCH o.administrador " +
                    "WHERE o.idOferta = :id",
                    OfertaMedica.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(o);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<OfertaMedica> findDisponiblesByEspecialidad(Integer idEspecialidad) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT o FROM OfertaMedica o " +
                    "JOIN FETCH o.medico m " +
                    "JOIN FETCH o.horario h " +
                    "JOIN FETCH o.especialidad e " +
                    "WHERE e.idEspecialidad = :espId " +
                    "AND o.estado = 'Disponible' " +
                    "AND h.fechaHoraInicio > :ahora " +
                    "ORDER BY h.fechaHoraInicio ASC",
                    OfertaMedica.class)
                    .setParameter("espId", idEspecialidad)
                    .setParameter("ahora", LocalDateTime.now())
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<OfertaMedica> findDisponiblesByEspecialidadYMedico(Integer idEspecialidad, Integer idMedico) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT o FROM OfertaMedica o " +
                    "JOIN FETCH o.medico m " +
                    "JOIN FETCH o.horario h " +
                    "JOIN FETCH o.especialidad e " +
                    "WHERE e.idEspecialidad = :espId " +
                    "AND m.idMedico = :medId " +
                    "AND o.estado = 'Disponible' " +
                    "AND h.fechaHoraInicio > :ahora " +
                    "ORDER BY h.fechaHoraInicio ASC",
                    OfertaMedica.class)
                    .setParameter("espId", idEspecialidad)
                    .setParameter("medId", idMedico)
                    .setParameter("ahora", LocalDateTime.now())
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<OfertaMedica> findAllConDetalles() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT o FROM OfertaMedica o " +
                    "JOIN FETCH o.medico " +
                    "JOIN FETCH o.horario " +
                    "JOIN FETCH o.especialidad " +
                    "ORDER BY o.horario.fechaHoraInicio DESC",
                    OfertaMedica.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
