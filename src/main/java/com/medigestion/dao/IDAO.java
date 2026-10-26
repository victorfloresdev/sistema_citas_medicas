package com.medigestion.dao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato base para operaciones de persistencia DAO (Principios SOLID: ISP y DIP).
 *
 * @param <T>  Tipo de entidad
 * @param <ID> Tipo del identificador
 */
public interface IDAO<T, ID> {
    T save(T entity);
    T update(T entity);
    void delete(ID id);
    Optional<T> findById(ID id);
    List<T> findAll();
}
