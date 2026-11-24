package com.medigestion.repository;

import com.medigestion.model.DemandaNoAtendida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemandaNoAtendidaRepository extends JpaRepository<DemandaNoAtendida, Integer> {
}
