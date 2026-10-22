package com.medigestion.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para la bitácora de eventos del sistema.
 * Tabla: bitacora_evento
 * Niveles: INFO, WARN, ERROR
 */
@Entity
@Table(name = "bitacora_evento")
public class BitacoraEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "origen", nullable = false, length = 50)
    private String origen;

    @Column(name = "nivel", nullable = false, length = 10)
    private String nivel = "INFO";

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "detalle", columnDefinition = "TEXT")
    private String detalle;

    public BitacoraEvento() {
        this.fecha = LocalDateTime.now();
    }

    public BitacoraEvento(String origen, String nivel, String mensaje) {
        this();
        this.origen = origen;
        this.nivel = nivel;
        this.mensaje = mensaje;
    }

    // Getters y Setters
    public Long getIdEvento() { return idEvento; }
    public void setIdEvento(Long idEvento) { this.idEvento = idEvento; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    @Override
    public String toString() {
        return "[" + fecha + "] " + nivel + " - " + origen + ": " + mensaje;
    }
}
