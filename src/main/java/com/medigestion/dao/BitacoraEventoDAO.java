package com.medigestion.dao;

import com.medigestion.model.BitacoraEvento;
import org.springframework.stereotype.Repository;

@Repository
public class BitacoraEventoDAO extends GenericDAO<BitacoraEvento, Long> implements IBitacoraEventoDAO {

    public BitacoraEventoDAO() {
        super(BitacoraEvento.class);
    }
}
