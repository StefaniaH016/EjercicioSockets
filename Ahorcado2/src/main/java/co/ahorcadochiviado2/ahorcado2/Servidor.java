package co.ahorcadochiviado2.ahorcado2;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Servidor implementa runnable{

    private static List<PrintWriter> clientes = new ArrayList<>();
    private static String palabra;
    private static StringBuilder palabraAleatoria;
    private static Set<Character> letrasUsadas = new HashSet<>();

    public static void main(String[] args) throws IOException {

        ServerSocket servidor = new ServerSocket(1500);
        System.out.println("Servidor iniciado... Esperando jugadores...");

        // Seleccionar una palabra aleatoria
        ArrayList<String> palabras = new ArrayList<>(Arrays.asList("casa", "pedorrera", "avion", "electromagnetismo", "hipotenusa", "arquitectura"));

public static void seleccionarPalabra( ArrayList<String> palabras) {
        Random random= new Random();
        palabra = palabras.get(random.nextInt(palabras.size()));
        palabraAleatoria = new StringBuilder("*".repeat(palabra.length()));
}
        while (true) {
            // Esperar a un nuevo cliente
            Socket clienteSocket = servidor.accept();
            System.out.println("Nuevo jugador conectado");
            PrintWriter out = new PrintWriter(clienteSocket.getOutputStream(), true);
            clientes.add(out);
            BufferedReader in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));

            // Crear hilo para manejar al cliente
            new Thread(new JugadorHandler(clienteSocket, in, out)).start();

            // Enviar la palabra enmascarada a todos los jugadores
            enviarPalabra();
        }
    }

    private static void enviarPalabra() {
        for (PrintWriter cliente : clientes) {
            cliente.println(palabraAleatoria.toString());
        }
    }

    // Método para verificar la letra enviada
    private static synchronized void procesarLetra(char letra) {
        if (!letrasUsadas.contains(letra)) {
            letrasUsadas.add(letra);
            boolean letraCorrecta = false;

            for (int i = 0; i < palabra.length(); i++) {
                if (palabra.charAt(i) == letra) {
                    palabraAleatoria.setCharAt(i, letra);
                    letraCorrecta = true;
                }
            }

            if (!letraCorrecta) {
                System.out.println("Letra incorrecta: " + letra);
            }

            // Verificar si la palabra fue adivinada
            if (palabraAleatoria.toString().equals(palabra)) {
                System.out.println("¡Palabra adivinada!");
                for (PrintWriter cliente : clientes) {
                    cliente.println("¡Felicidades, la palabra es: " + palabra);
                }
                cerrarConexiones();
            }

            // Enviar la palabra actualizada a todos los clientes
            enviarPalabra();
        }
    }

    private static void cerrarConexiones() {
        for (PrintWriter cliente : clientes) {
            cliente.close();
        }
        clientes.clear();
    }

    // Maneja la conexión de cada jugador
    static class JugadorHandler implements Runnable {
        private Socket clienteSocket;
        private BufferedReader in;
        private PrintWriter out;

        public JugadorHandler(Socket socket, BufferedReader in, PrintWriter out) {
            this.clienteSocket = socket;
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            try {
                out.println("¡Bienvenido al juego del Ahorcado!");
                while (true) {
                    String input = in.readLine();
                    if (input != null && input.length() == 1) {
                        char letra = input.charAt(0);
                        procesarLetra(letra);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
