package com.medigestion.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una reserva de cita médica.
 * Tabla: reserva_cita
 * Estados: Pendiente, Confirmada, Cancelada, Completada, Ausente, Reprogramada
 */
@Entity
@Table(name = "reserva_cita")
public class ReservaCita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Integer idReserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_oferta", nullable = false)
    private OfertaMedica ofertaMedica;

    @Column(name = "codigo_ticket", nullable = false, unique = true, length = 20)
    private String codigoTicket;

    @Column(name = "fecha_reserva", nullable = false)
    private LocalDateTime fechaReserva;

    @Column(name = "estado", nullable = false, length = 15)
    private String estado = "Pendiente";

    @Column(name = "turno", nullable = false, length = 20)
    private String turno;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_presentacion")
    private LocalDateTime fechaPresentacion;

    @Column(name = "fecha_llamado")
    private LocalDateTime fechaLlamado;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "canal_cancelacion", length = 15)
    private String canalCancelacion;

    @Column(name = "motivo_cancelacion")
    private String motivoCancelacion;

    public ReservaCita() {
        this.fechaReserva = LocalDateTime.now();
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public OfertaMedica getOfertaMedica() { return ofertaMedica; }
    public void setOfertaMedica(OfertaMedica ofertaMedica) { this.ofertaMedica = ofertaMedica; }

    public String getCodigoTicket() { return codigoTicket; }
    public void setCodigoTicket(String codigoTicket) { this.codigoTicket = codigoTicket; }

    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaPresentacion() { return fechaPresentacion; }
    public void setFechaPresentacion(LocalDateTime fechaPresentacion) { this.fechaPresentacion = fechaPresentacion; }

    public LocalDateTime getFechaLlamado() { return fechaLlamado; }
    public void setFechaLlamado(LocalDateTime fechaLlamado) { this.fechaLlamado = fechaLlamado; }

    public LocalDateTime getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(LocalDateTime fechaCancelacion) { this.fechaCancelacion = fechaCancelacion; }

    public String getCanalCancelacion() { return canalCancelacion; }
    public void setCanalCancelacion(String canalCancelacion) { this.canalCancelacion = canalCancelacion; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    @Override
    public String toString() {
        return "ReservaCita{id=" + idReserva + ", ticket='" + codigoTicket + "', estado='" + estado + "'}";
    }
}
