import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.List;
import java.util.function.Consumer;

public class Validacontramod {

    private static final String PATRON_CONTRASENA =
            "^(?=(?:.*[A-Z]){2,})(?=(?:.*[a-z]){3,})(?=(?:.*\\d){1,})(?=.*[@#$%^&+=!])(?=.{8,}).*$";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Thread> hilos = new ArrayList<>();
        List<String> resultados = new ArrayList<>();

        System.out.println("Ingrese el número de contraseñas a validar: ");
        int numContrasenas = leerEntero(scanner);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("registro_contrasenas.txt", true))) {
            for (int i = 0; i < numContrasenas; i++) {
                System.out.println("Ingrese la contraseña #" + (i + 1) + ": ");
                String contrasena = scanner.nextLine();

                // Crear el hilo para validar la contraseña y almacenar el resultado
                Thread hilo = new Thread(new ValidadorContrasena(contrasena, resultados, writer));
                hilos.add(hilo);
                hilo.start();
            }

            for (Thread hilo : hilos) {
                try {
                    hilo.join();
                } catch (InterruptedException e) {
                    System.out.println("Error al esperar la finalización de un hilo: " + e.getMessage());
                }
            }

            resultados.forEach((Consumer<String>) resultado -> {
                try {
                    writer.write(resultado + "\n");
                } catch (IOException e) {
                    System.out.println("Error al escribir en el archivo: " + e.getMessage());
                }
            });

            System.out.println("Validación de contraseñas finalizada y resultados guardados en el archivo.");
        } catch (IOException e) {
            System.out.println("Error al crear o escribir en el archivo de registro: " + e.getMessage());
        }

        scanner.close();
    }

    private static int leerEntero(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Por favor, ingrese un número entero:");
            }
        }
    }

    static class ValidadorContrasena implements Runnable {
        private final String contrasena;
        private final List<String> resultados;
        private final BufferedWriter writer;

        public ValidadorContrasena(String contrasena, List<String> resultados, BufferedWriter writer) {
            this.contrasena = contrasena;
            this.resultados = resultados;
            this.writer = writer;
        }

        @Override
        public void run() {
            boolean esValida = validarContrasena(contrasena);
            String resultado = "Contraseña: \"" + contrasena + "\" -> " + (esValida ? "VÁLIDA" : "INVÁLIDA");

            resultados.add(resultado);

            try {
                writer.write(resultado + "\n");
            } catch (IOException e) {
                System.out.println("Error al escribir en el archivo: " + e.getMessage());
            }
        }

        private boolean validarContrasena(String contrasena) {
            return Pattern.matches(PATRON_CONTRASENA, contrasena);
        }
    }
}
