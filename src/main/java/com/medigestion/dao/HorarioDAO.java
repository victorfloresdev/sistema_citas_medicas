package com.medigestion.dao;

import com.medigestion.model.Horario;
import com.medigestion.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class HorarioDAO extends GenericDAO<Horario, Integer> {

    public HorarioDAO() {
        super(Horario.class);
    }

    public List<Horario> findByRango(LocalDateTime inicio, LocalDateTime fin) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT h FROM Horario h WHERE h.fechaHoraInicio >= :inicio AND h.fechaHoraFin <= :fin ORDER BY h.fechaHoraInicio",
                    Horario.class)
                    .setParameter("inicio", inicio)
                    .setParameter("fin", fin)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
