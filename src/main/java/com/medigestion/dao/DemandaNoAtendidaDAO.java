package com.medigestion.dao;

import com.medigestion.model.DemandaNoAtendida;
import org.springframework.stereotype.Repository;

@Repository
public class DemandaNoAtendidaDAO extends GenericDAO<DemandaNoAtendida, Integer> implements IDemandaNoAtendidaDAO {

    public DemandaNoAtendidaDAO() {
        super(DemandaNoAtendida.class);
    }
}
