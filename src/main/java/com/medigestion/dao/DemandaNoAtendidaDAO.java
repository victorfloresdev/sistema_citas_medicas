package com.medigestion.dao;

import com.medigestion.model.DemandaNoAtendida;

public class DemandaNoAtendidaDAO extends GenericDAO<DemandaNoAtendida, Integer> implements IDemandaNoAtendidaDAO {

    public DemandaNoAtendidaDAO() {
        super(DemandaNoAtendida.class);
    }
}
