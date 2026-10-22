package com.medigestion.model;

import jakarta.persistence.*;

/**
 * Entidad que representa la oferta médica (slot de cita disponible).
 * Relaciona un médico con un horario y una especialidad.
 * Tabla: oferta_medica
 */
@Entity
@Table(name = "oferta_medica")
public class OfertaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oferta")
    private Integer idOferta;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado = "Disponible";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medico", nullable = false)
    private Medico medico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_horario", nullable = false)
    private Horario horario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_especialidad", nullable = false)
    private Especialidad especialidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Administrador administrador;

    @Column(name = "consultorio", nullable = false, length = 10)
    private String consultorio;

    public OfertaMedica() {}

    // Getters y Setters
    public Integer getIdOferta() { return idOferta; }
    public void setIdOferta(Integer idOferta) { this.idOferta = idOferta; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public Horario getHorario() { return horario; }
    public void setHorario(Horario horario) { this.horario = horario; }

    public Especialidad getEspecialidad() { return especialidad; }
    public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }

    public Administrador getAdministrador() { return administrador; }
    public void setAdministrador(Administrador administrador) { this.administrador = administrador; }

    public String getConsultorio() { return consultorio; }
    public void setConsultorio(String consultorio) { this.consultorio = consultorio; }

    @Override
    public String toString() {
        return "OfertaMedica{id=" + idOferta + ", estado='" + estado + "', consultorio='" + consultorio + "'}";
    }
}
