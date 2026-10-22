package com.medigestion.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Gestor Singleton de conexiones JPA/Hibernate.
 * Intenta conectar a PostgreSQL (medigestionPU).
 * Si PostgreSQL no está corriendo en localhost:5432, conmuta a modo demostración en memoria (medigestionH2PU)
 * para garantizar la ejecución inmediata y continua del sistema.
 */
public class HibernateUtil {

    private static final EntityManagerFactory emf;
    private static final boolean usandoPostgres;

    static {
        EntityManagerFactory tempEmf = null;
        boolean postgresConectado = false;

        // Comprobar si PostgreSQL está escuchando en puerto 5432
        if (estaPostgresDisponible("localhost", 5432, 1000)) {
            try {
                tempEmf = Persistence.createEntityManagerFactory("medigestionPU");
                postgresConectado = true;
                System.out.println("  ✔ Conectado exitosamente a PostgreSQL (localhost:5432/medigestion).");
            } catch (Exception e) {
                System.out.println("  ⚠ No se pudo conectar a PostgreSQL: " + e.getMessage());
            }
        } else {
            System.out.println("  ℹ PostgreSQL (localhost:5432) no está activo en este momento.");
        }

        // Si PostgreSQL no estuvo disponible, usar unidad de persistencia en memoria
        if (tempEmf == null) {
            System.out.println("  ✔ Inicializando Hibernate en modo compatibilidad (H2/PostgreSQL Mode) para demostración continua.");
            tempEmf = Persistence.createEntityManagerFactory("medigestionH2PU");
        }

        emf = tempEmf;
        usandoPostgres = postgresConectado;
    }

    private static boolean estaPostgresDisponible(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static boolean isUsandoPostgres() {
        return usandoPostgres;
    }

    public static void shutdown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
