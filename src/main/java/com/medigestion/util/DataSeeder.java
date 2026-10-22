package com.medigestion.util;

import com.medigestion.dao.*;
import com.medigestion.model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Semillero de datos iniciales idénticos a los prototipos de MediGestión.
 */
public class DataSeeder {

    public static void seed() {
        EspecialidadDAO espDAO = new EspecialidadDAO();
        if (!espDAO.findAll().isEmpty()) {
            return; // Ya inicializado
        }

        System.out.println("  [DataSeeder] Inicializando datos maestros de MediGestión...");

        // 1. Especialidades
        Especialidad cardio = espDAO.save(new Especialidad("Cardiología"));
        Especialidad ped = espDAO.save(new Especialidad("Pediatría"));
        Especialidad orto = espDAO.save(new Especialidad("Ortopedia"));
        Especialidad neuro = espDAO.save(new Especialidad("Neurología"));
        Especialidad dermato = espDAO.save(new Especialidad("Dermatología"));

        // 2. Administrador
        AdministradorDAO adminDAO = new AdministradorDAO();
        Administrador admin = new Administrador();
        admin.setNombreAdmin("Administrador MediGestión");
        admin.setEmail("admin@medigestion.com");
        admin.setContrasenaHash(BCrypt.hashpw("admin123", BCrypt.gensalt()));
        admin.setRol("ADMIN");
        admin.setActivo(true);
        admin = adminDAO.save(admin);

        // 3. Médicos (según las pantallas del prototipo)
        MedicoDAO medDAO = new MedicoDAO();

        Medico m1 = new Medico();
        m1.setNombres("Sarah");
        m1.setApellidos("Jenkins");
        m1.setEmail("sarah.jenkins@medigestion.com");
        m1.setConsultorio("C-101");
        m1.setContrasenaHash(BCrypt.hashpw("medico123", BCrypt.gensalt()));
        m1 = medDAO.save(m1);

        Medico m2 = new Medico();
        m2.setNombres("Marcus");
        m2.setApellidos("Thorne");
        m2.setEmail("marcus.thorne@medigestion.com");
        m2.setConsultorio("C-102");
        m2.setContrasenaHash(BCrypt.hashpw("medico123", BCrypt.gensalt()));
        m2 = medDAO.save(m2);

        Medico m3 = new Medico();
        m3.setNombres("Michael");
        m3.setApellidos("Chen");
        m3.setEmail("michael.chen@medigestion.com");
        m3.setConsultorio("P-201");
        m3.setContrasenaHash(BCrypt.hashpw("medico123", BCrypt.gensalt()));
        m3 = medDAO.save(m3);

        Medico m4 = new Medico();
        m4.setNombres("Emily");
        m4.setApellidos("Rodriguez");
        m4.setEmail("emily.rodriguez@medigestion.com");
        m4.setConsultorio("O-301");
        m4.setContrasenaHash(BCrypt.hashpw("medico123", BCrypt.gensalt()));
        m4 = medDAO.save(m4);

        Medico m5 = new Medico();
        m5.setNombres("Robert");
        m5.setApellidos("Vance");
        m5.setEmail("robert.vance@medigestion.com");
        m5.setConsultorio("N-105");
        m5.setContrasenaHash(BCrypt.hashpw("medico123", BCrypt.gensalt()));
        m5 = medDAO.save(m5);

        Medico m6 = new Medico();
        m6.setNombres("Anita");
        m6.setApellidos("Patel");
        m6.setEmail("anita.patel@medigestion.com");
        m6.setConsultorio("D-204");
        m6.setContrasenaHash(BCrypt.hashpw("medico123", BCrypt.gensalt()));
        m6 = medDAO.save(m6);

        // 4. Paciente demo (DNI 45892134)
        PacienteDAO pacDAO = new PacienteDAO();
        Paciente pacDemo = new Paciente();
        pacDemo.setDni("45892134");
        pacDemo.setNombres("Carlos");
        pacDemo.setApellidos("Mendoza Rivera");
        pacDemo.setEdad((short) 34);
        pacDemo.setSexo("HOMBRE");
        pacDemo.setTelefono("987654321");
        pacDemo.setEmail("carlos.mendoza@email.com");
        pacDemo.setContrasenaHash(BCrypt.hashpw("paciente123", BCrypt.gensalt()));
        pacDemo.setDireccion("Av. Las Palmeras 450, Lima");
        pacDAO.save(pacDemo);

        // 5. Horarios y Ofertas Médicas
        HorarioDAO horDAO = new HorarioDAO();
        OfertaMedicaDAO ofDAO = new OfertaMedicaDAO();

        LocalDate hoy = LocalDate.now();
        LocalDate manana = hoy.plusDays(1);
        LocalDate pasado = hoy.plusDays(2);

        // Turnos Hoy
        crearOferta(horDAO, ofDAO, m1, cardio, admin, hoy, LocalTime.of(9, 0), LocalTime.of(9, 30));
        crearOferta(horDAO, ofDAO, m1, cardio, admin, hoy, LocalTime.of(10, 30), LocalTime.of(11, 0));
        crearOferta(horDAO, ofDAO, m1, cardio, admin, hoy, LocalTime.of(11, 30), LocalTime.of(12, 0));
        crearOferta(horDAO, ofDAO, m1, cardio, admin, hoy, LocalTime.of(14, 15), LocalTime.of(14, 45));

        crearOferta(horDAO, ofDAO, m2, cardio, admin, manana, LocalTime.of(10, 0), LocalTime.of(10, 30));
        crearOferta(horDAO, ofDAO, m2, cardio, admin, manana, LocalTime.of(13, 0), LocalTime.of(13, 30));

        crearOferta(horDAO, ofDAO, m3, ped, admin, hoy, LocalTime.of(14, 15), LocalTime.of(14, 45));
        crearOferta(horDAO, ofDAO, m4, orto, admin, hoy, LocalTime.of(11, 0), LocalTime.of(11, 30));
        crearOferta(horDAO, ofDAO, m5, neuro, admin, hoy, LocalTime.of(9, 45), LocalTime.of(10, 15));
        crearOferta(horDAO, ofDAO, m6, dermato, admin, pasado, LocalTime.of(8, 30), LocalTime.of(9, 0));

        System.out.println("  [DataSeeder] Datos maestros inicializados correctamente.\n");
    }

    private static void crearOferta(HorarioDAO hDAO, OfertaMedicaDAO oDAO, 
                                   Medico medico, Especialidad esp, Administrador admin,
                                   LocalDate fecha, LocalTime inicio, LocalTime fin) {
        Horario h = new Horario(LocalDateTime.of(fecha, inicio), LocalDateTime.of(fecha, fin));
        h = hDAO.save(h);

        OfertaMedica of = new OfertaMedica();
        of.setMedico(medico);
        of.setEspecialidad(esp);
        of.setHorario(h);
        of.setAdministrador(admin);
        of.setConsultorio(medico.getConsultorio());
        of.setEstado("Disponible");
        oDAO.save(of);
    }
}
