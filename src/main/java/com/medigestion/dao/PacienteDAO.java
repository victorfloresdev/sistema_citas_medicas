package com.medigestion.dao;

import com.medigestion.model.Paciente;
import com.medigestion.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Optional;

public class PacienteDAO extends GenericDAO<Paciente, String> {

    public PacienteDAO() {
        super(Paciente.class);
    }

    public Optional<Paciente> findByDni(String dni) {
        return findById(dni);
    }

    public Optional<Paciente> findByEmail(String email) {
        EntityManager em = HibernateUtil.getEntityManager();
        try {
            Paciente p = em.createQuery("SELECT p FROM Paciente p WHERE p.email = :email", Paciente.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(p);
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
}
