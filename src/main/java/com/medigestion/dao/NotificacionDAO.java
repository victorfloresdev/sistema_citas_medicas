package com.medigestion.dao;

import com.medigestion.model.Notificacion;
import com.medigestion.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

public class NotificacionDAO extends GenericDAO<Notificacion, Integer> {

    public NotificacionDAO() {
        super(Notificacion.class);
    }

    public List<Notificacion> findPendientes() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT n FROM Notificacion n WHERE n.estadoEnvio = 'Pendiente' ORDER BY n.fechaCreacion ASC",
                    Notificacion.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
