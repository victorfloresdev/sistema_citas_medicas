package com.medigestion.controller;

import com.medigestion.controller.web.SalaEsperaWebController;
import com.medigestion.dao.ReservaCitaDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de integración de capa Web MVC con MockMvc (Hito 60% - Rúbrica: Pruebas de Software).
 * Valida el enrutamiento HTTP, la inyección del modelo y la resolución de la vista Thymeleaf.
 */
class SalaEsperaWebControllerTest {

    private MockMvc mockMvc;
    private ReservaCitaDAO reservaCitaDAO;

    @BeforeEach
    void setUp() {
        reservaCitaDAO = Mockito.mock(ReservaCitaDAO.class);
        SalaEsperaWebController controller = new SalaEsperaWebController(reservaCitaDAO);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /sala-espera debe responder HTTP 200 y renderizar vista medico/sala-espera")
    void testPantallaSalaEsperaRetornaVistaCorrecta() throws Exception {
        Mockito.when(reservaCitaDAO.findUltimasCitasLlamadas(any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/sala-espera"))
                .andExpect(status().isOk())
                .andExpect(view().name("medico/sala-espera"))
                .andExpect(model().attributeExists("totalLlamados"))
                .andExpect(model().attribute("totalLlamados", 0));
    }
}
