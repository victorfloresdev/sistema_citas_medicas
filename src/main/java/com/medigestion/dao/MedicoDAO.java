package com.medigestion.dao;

import com.medigestion.model.Medico;
import com.medigestion.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

public class MedicoDAO extends GenericDAO<Medico, Integer> {

    public MedicoDAO() {
        super(Medico.class);
    }

    public List<Medico> findActivos() {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            return em.createQuery("SELECT m FROM Medico m WHERE m.activo = true", Medico.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
