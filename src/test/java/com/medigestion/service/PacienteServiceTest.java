package com.medigestion.service;

import com.medigestion.dao.IBitacoraEventoDAO;
import com.medigestion.dao.IPacienteDAO;
import com.medigestion.model.BitacoraEvento;
import com.medigestion.model.Paciente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias y de Caja Blanca para PacienteService (RF-01, RNF-04).
 * Cobertura de caminos de validación con Apache Commons Validator y BCrypt.
 */
@ExtendWith(MockitoExtension.class)
public class PacienteServiceTest {

    @Mock
    private IPacienteDAO pacienteDAO;

    @Mock
    private IBitacoraEventoDAO bitacoraDAO;

    private PacienteService pacienteService;

    @BeforeEach
    void setUp() {
        pacienteService = new PacienteService(pacienteDAO, bitacoraDAO);
    }

    @Test
    @DisplayName("Caja Blanca Paciente - Camino 1: DNI inválido (longitud incorrecta o letras) lanza excepción")
    void testIdentificarPaciente_DniInvalido() {
        assertThatThrownBy(() -> pacienteService.identificarPaciente("12345"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8 dígitos numéricos");

        assertThatThrownBy(() -> pacienteService.identificarPaciente("ABC12345"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8 dígitos numéricos");
    }

    @Test
    @DisplayName("Caja Blanca Paciente - Camino 2: Rechazar registro de paciente con DNI duplicado")
    void testRegistrarPaciente_DniDuplicado() {
        when(pacienteDAO.findByDni("45892134")).thenReturn(Optional.of(new Paciente()));

        assertThatThrownBy(() -> pacienteService.registrarPaciente(
                "45892134", "Carlos", "Mendoza", (short) 35, "HOMBRE", "987654321",
                "carlos@email.com", "Password123", "Av. Principal 123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya se encuentra registrado");

        verify(pacienteDAO, never()).save(any());
    }

    @Test
    @DisplayName("Caja Blanca Paciente - Camino 3: Rechazar correo electrónico inválido mediante Apache Commons")
    void testRegistrarPaciente_EmailInvalido() {
        assertThatThrownBy(() -> pacienteService.registrarPaciente(
                "77889900", "Ana", "Torres", (short) 28, "MUJER", "912345678",
                "correo-invalido-sin-arroba", "claveSegura123", "Calle Los Pinos 45"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("formato del correo electrónico es inválido");

        verify(pacienteDAO, never()).save(any());
    }

    @Test
    @DisplayName("Caja Blanca Paciente - Camino 4: Registro exitoso con hash seguro BCrypt (RNF-04)")
    void testRegistrarPaciente_RegistroExitoso() {
        when(pacienteDAO.findByDni("77889900")).thenReturn(Optional.empty());
        when(pacienteDAO.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Paciente resultado = pacienteService.registrarPaciente(
                "77889900", "ana maria", "torres lopez", (short) 28, "MUJER", "912345678",
                "ana.torres@gmail.com", "ClaveSecreta99", "Calle Los Pinos 45");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getDni()).isEqualTo("77889900");
        assertThat(resultado.getNombres()).isEqualTo("Ana maria");
        assertThat(resultado.getApellidos()).isEqualTo("Torres lopez");
        assertThat(resultado.getContrasenaHash()).isNotNull();
        assertThat(resultado.getContrasenaHash()).startsWith("$2a$12$");

        // Validar autenticación de clave cifrada
        boolean authOk = pacienteService.verificarContrasena(resultado, "ClaveSecreta99");
        boolean authFail = pacienteService.verificarContrasena(resultado, "ClaveIncorrecta");

        assertThat(authOk).isTrue();
        assertThat(authFail).isFalse();

        verify(pacienteDAO).save(any(Paciente.class));
        verify(bitacoraDAO).save(any(BitacoraEvento.class));
    }
}
