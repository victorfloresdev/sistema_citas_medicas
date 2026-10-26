package com.medigestion.dao;

import com.medigestion.model.BitacoraEvento;

public class BitacoraEventoDAO extends GenericDAO<BitacoraEvento, Long> implements IBitacoraEventoDAO {

    public BitacoraEventoDAO() {
        super(BitacoraEvento.class);
    }
}
