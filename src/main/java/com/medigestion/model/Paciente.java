package com.medigestion.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa un paciente del sistema.
 * Tabla: paciente
 * PK: dni (CHAR 8 dígitos)
 */
@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @Column(name = "dni", length = 8, nullable = false)
    private String dni;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "edad", nullable = false)
    private Short edad;

    @Column(name = "sexo", nullable = false, length = 10)
    private String sexo;

    @Column(name = "telefono", nullable = false, unique = true, length = 9)
    private String telefono;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "contrasena_hash", nullable = false, length = 255)
    private String contrasenaHash;

    @Column(name = "direccion", length = 150)
    private String direccion;

    @Column(name = "telegram_chat_id")
    private Long telegramChatId;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    public Paciente() {
        this.fechaRegistro = LocalDateTime.now();
    }

    // Getters y Setters
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public Short getEdad() { return edad; }
    public void setEdad(Short edad) { this.edad = edad; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public Long getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(Long telegramChatId) { this.telegramChatId = telegramChatId; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    @Override
    public String toString() {
        return "Paciente{dni='" + dni + "', nombre='" + getNombreCompleto() + "', email='" + email + "'}";
    }
}
