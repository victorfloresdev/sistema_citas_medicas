package com.medigestion.model;

import jakarta.persistence.*;

/**
 * Entidad que representa un administrador del sistema.
 * Tabla: administrador
 * Roles: ADMIN, VENTANILLA
 */
@Entity
@Table(name = "administrador")
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_admin")
    private Integer idAdmin;

    @Column(name = "nombre_admin", nullable = false, length = 100)
    private String nombreAdmin;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @Column(name = "rol", nullable = false, length = 20)
    private String rol = "ADMIN";

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public Administrador() {}

    // Getters y Setters
    public Integer getIdAdmin() { return idAdmin; }
    public void setIdAdmin(Integer idAdmin) { this.idAdmin = idAdmin; }

    public String getNombreAdmin() { return nombreAdmin; }
    public void setNombreAdmin(String nombreAdmin) { this.nombreAdmin = nombreAdmin; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "Administrador{id=" + idAdmin + ", nombre='" + nombreAdmin + "', rol='" + rol + "'}";
    }
}
