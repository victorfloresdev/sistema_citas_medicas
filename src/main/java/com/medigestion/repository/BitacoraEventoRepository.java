package com.medigestion.repository;

import com.medigestion.model.BitacoraEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraEventoRepository extends JpaRepository<BitacoraEvento, Long> {
}
