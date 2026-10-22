package com.medigestion.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para registrar demanda no atendida cuando no hay horarios disponibles.
 * Tabla: demanda_no_atendida
 */
@Entity
@Table(name = "demanda_no_atendida")
public class DemandaNoAtendida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demanda")
    private Integer idDemanda;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_especialidad", nullable = false)
    private Especialidad especialidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dni", nullable = false)
    private Paciente paciente;

    public DemandaNoAtendida() {
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters y Setters
    public Integer getIdDemanda() { return idDemanda; }
    public void setIdDemanda(Integer idDemanda) { this.idDemanda = idDemanda; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    @Override
    public String toString() {
        return "DemandaNoAtendida{id=" + idDemanda + ", fecha=" + fechaRegistro + "}";
    }
}
