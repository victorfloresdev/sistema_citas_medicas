package com.medigestion.dao;

import com.medigestion.model.Especialidad;
import com.medigestion.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Optional;

public class EspecialidadDAO extends GenericDAO<Especialidad, Integer> {

    public EspecialidadDAO() {
        super(Especialidad.class);
    }

    public Optional<Especialidad> findByNombre(String nombre) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Especialidad esp = em.createQuery(
                    "SELECT e FROM Especialidad e WHERE LOWER(e.nombreEspecialidad) = LOWER(:nombre)",
                    Especialidad.class)
                    .setParameter("nombre", nombre)
                    .getSingleResult();
            return Optional.of(esp);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
}
