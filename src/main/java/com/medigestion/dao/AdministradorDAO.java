package com.medigestion.dao;

import com.medigestion.model.Administrador;
import com.medigestion.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Optional;

public class AdministradorDAO extends GenericDAO<Administrador, Integer> {

    public AdministradorDAO() {
        super(Administrador.class);
    }

    public Optional<Administrador> findByEmail(String email) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Administrador a = em.createQuery(
                    "SELECT a FROM Administrador a WHERE a.email = :email AND a.activo = true",
                    Administrador.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(a);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
}
