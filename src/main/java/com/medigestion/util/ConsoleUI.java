package com.medigestion.util;

import java.util.Scanner;

/**
 * Clase utilitaria para la interfaz de consola.
 * Proporciona métodos para mostrar menús, leer entradas y formatear salida.
 */
public class ConsoleUI {

    private static final Scanner scanner = new Scanner(System.in);
    private static final String LINEA = "═".repeat(60);
    private static final String LINEA_SIMPLE = "─".repeat(60);

    public static void limpiarPantalla() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void mostrarEncabezado(String titulo) {
        System.out.println();
        System.out.println("╔" + LINEA + "╗");
        System.out.printf("║ %-58s ║%n", titulo);
        System.out.println("╚" + LINEA + "╝");
    }

    public static void mostrarSubtitulo(String subtitulo) {
        System.out.println("┌" + LINEA_SIMPLE + "┐");
        System.out.printf("│ %-58s │%n", subtitulo);
        System.out.println("└" + LINEA_SIMPLE + "┘");
    }

    public static void mostrarInfo(String mensaje) {
        System.out.println("  ℹ  " + mensaje);
    }

    public static void mostrarExito(String mensaje) {
        System.out.println("  ✔  " + mensaje);
    }

    public static void mostrarError(String mensaje) {
        System.out.println("  ✖  " + mensaje);
    }

    public static void mostrarAdvertencia(String mensaje) {
        System.out.println("  ⚠  " + mensaje);
    }

    public static void mostrarSeparador() {
        System.out.println("  " + LINEA_SIMPLE);
    }

    public static int leerOpcion(String prompt, int min, int max) {
        while (true) {
            System.out.print("\n  ▶ " + prompt + " [" + min + "-" + max + "]: ");
            try {
                String input = scanner.nextLine().trim();
                int opcion = Integer.parseInt(input);
                if (opcion >= min && opcion <= max) {
                    return opcion;
                }
                mostrarError("Opcion fuera de rango. Intente de nuevo.");
            } catch (NumberFormatException e) {
                mostrarError("Entrada invalida. Ingrese un numero.");
            }
        }
    }

    public static String leerTexto(String prompt) {
        System.out.print("  ▶ " + prompt + ": ");
        return scanner.nextLine().trim();
    }

    public static String leerTextoObligatorio(String prompt) {
        while (true) {
            String texto = leerTexto(prompt);
            if (!texto.isEmpty()) {
                return texto;
            }
            mostrarError("Este campo es obligatorio.");
        }
    }

    public static String leerDni() {
        while (true) {
            String dni = leerTexto("Ingrese DNI (8 digitos)");
            if (dni.matches("^[0-9]{8}$")) {
                return dni;
            }
            mostrarError("DNI invalido. Debe tener exactamente 8 digitos numericos.");
        }
    }

    public static String leerTelefono() {
        while (true) {
            String tel = leerTexto("Ingrese telefono (9 digitos)");
            if (tel.matches("^[0-9]{9}$")) {
                return tel;
            }
            mostrarError("Telefono invalido. Debe tener exactamente 9 digitos.");
        }
    }

    public static String leerEmail() {
        while (true) {
            String email = leerTexto("Ingrese email");
            if (email.contains("@") && email.contains(".")) {
                return email;
            }
            mostrarError("Email invalido.");
        }
    }

    public static boolean confirmar(String pregunta) {
        System.out.print("  ▶ " + pregunta + " (S/N): ");
        String respuesta = scanner.nextLine().trim().toUpperCase();
        return respuesta.equals("S") || respuesta.equals("SI");
    }

    public static void pausar() {
        System.out.print("\n  Presione ENTER para continuar...");
        scanner.nextLine();
    }

    public static void mostrarMenu(String titulo, String... opciones) {
        mostrarEncabezado(titulo);
        System.out.println();
        for (int i = 0; i < opciones.length; i++) {
            System.out.printf("    [%d] %s%n", i + 1, opciones[i]);
        }
        System.out.println();
    }

    public static void mostrarTabla(String[] cabeceras, String[][] datos) {
        int[] anchos = new int[cabeceras.length];
        for (int i = 0; i < cabeceras.length; i++) {
            anchos[i] = cabeceras[i].length();
        }
        for (String[] fila : datos) {
            for (int i = 0; i < fila.length && i < anchos.length; i++) {
                if (fila[i] != null && fila[i].length() > anchos[i]) {
                    anchos[i] = fila[i].length();
                }
            }
        }

        // Cabecera
        StringBuilder sb = new StringBuilder("  ");
        for (int i = 0; i < cabeceras.length; i++) {
            sb.append(String.format("%-" + (anchos[i] + 2) + "s", cabeceras[i]));
        }
        System.out.println(sb);

        sb = new StringBuilder("  ");
        for (int i = 0; i < cabeceras.length; i++) {
            sb.append("─".repeat(anchos[i] + 2));
        }
        System.out.println(sb);

        // Datos
        for (String[] fila : datos) {
            sb = new StringBuilder("  ");
            for (int i = 0; i < cabeceras.length; i++) {
                String valor = (i < fila.length && fila[i] != null) ? fila[i] : "";
                sb.append(String.format("%-" + (anchos[i] + 2) + "s", valor));
            }
            System.out.println(sb);
        }
    }
}
