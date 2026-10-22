package com.medigestion.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una notificación enviada al paciente.
 * Tabla: notificacion
 * Tipos: Confirmacion, Recordatorio, Cancelacion, Reprogramacion, AlertaDisponibilidad
 */
@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_reserva")
    private ReservaCita reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_suscripcion")
    private SuscripcionAlerta suscripcion;

    @Column(name = "tipo", nullable = false, length = 25)
    private String tipo;

    @Column(name = "canal", nullable = false, length = 10)
    private String canal = "Telegram";

    @Column(name = "telefono_destino", nullable = false, length = 9)
    private String telefonoDestino;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "estado_envio", nullable = false, length = 15)
    private String estadoEnvio = "Pendiente";

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "detalle_error", columnDefinition = "TEXT")
    private String detalleError;

    public Notificacion() {
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Integer getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(Integer idNotificacion) { this.idNotificacion = idNotificacion; }

    public ReservaCita getReserva() { return reserva; }
    public void setReserva(ReservaCita reserva) { this.reserva = reserva; }

    public SuscripcionAlerta getSuscripcion() { return suscripcion; }
    public void setSuscripcion(SuscripcionAlerta suscripcion) { this.suscripcion = suscripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }

    public String getTelefonoDestino() { return telefonoDestino; }
    public void setTelefonoDestino(String telefonoDestino) { this.telefonoDestino = telefonoDestino; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getEstadoEnvio() { return estadoEnvio; }
    public void setEstadoEnvio(String estadoEnvio) { this.estadoEnvio = estadoEnvio; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public String getDetalleError() { return detalleError; }
    public void setDetalleError(String detalleError) { this.detalleError = detalleError; }

    @Override
    public String toString() {
        return "Notificacion{id=" + idNotificacion + ", tipo='" + tipo + "', estado='" + estadoEnvio + "'}";
    }
}
