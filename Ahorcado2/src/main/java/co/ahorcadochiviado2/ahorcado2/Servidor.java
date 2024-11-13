package co.ahorcadochiviado2.ahorcado2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Servidor implements Runnable {
    private JFrame frame;
    private JButton btnEnviarPalabra;
    private JLabel lblPalabra;
    private String palabraOculta;
    private String palabraOriginal;
    private ArrayList<String> listaPalabras;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private ArrayList<ClienteHandler> clientesConectados;

    // Constructor
    public Servidor() {
        listaPalabras = new ArrayList<>();
        listaPalabras.add("CONEJO");
        listaPalabras.add("PERRO");
        listaPalabras.add("OBSERVABLE");
        listaPalabras.add("CLANMAMASITA");

        clientesConectados = new ArrayList<>();
        
        // Configuración de la interfaz gráfica
        frame = new JFrame("Juego de Ahorcado");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());
        
        lblPalabra = new JLabel("Palabra");
        frame.add(lblPalabra);
        
        btnEnviarPalabra = new JButton("Enviar Palabra");
        btnEnviarPalabra.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enviarPalabra();
            }
        });
        frame.add(btnEnviarPalabra);
        
        frame.setVisible(true);
    }
    
     // Método para enviar una palabra al azar
    public void enviarPalabra() {
        // Elegir una palabra al azar
        Random rand = new Random();
        palabraOriginal = listaPalabras.get(rand.nextInt(listaPalabras.size()));
        
        // Ocultar la palabra (reemplazar letras por '*')
        palabraOculta = palabraOriginal.replaceAll(".", "*");

        lblPalabra.setText(palabraOculta);

        for (ClienteHandler cliente : clientesConectados) {
            cliente.enviarPalabraOculta(palabraOculta);

        }
    }

    // Método para verificar si una letra enviada por el cliente es correcta
    public void verificarLetra(char letra) {
        StringBuilder nuevaPalabra = new StringBuilder(palabraOculta);
        boolean letraCorrecta = false;
        
        // Recorrer la palabra original para encontrar coincidencias
        for (int i = 0; i < palabraOriginal.length(); i++) {
            if (palabraOriginal.charAt(i) == letra) {
                nuevaPalabra.setCharAt(i, letra); // Reemplazar '*' por la letra correcta
                letraCorrecta = true;
            }
        }

        if (!letraCorrecta){
            System.out.println("la letra ingresada no esta en la palabra");
        }

        palabraOculta = nuevaPalabra.toString();
        lblPalabra.setText(palabraOculta);

        for (ClienteHandler cliente : clientesConectados) {
            cliente.enviarPalabraOculta(palabraOculta);
        }

        // Si no hay asteriscos, la palabra está completa
        if (!palabraOculta.contains("*")) {
            // Notificar a todos los clientes que la palabra ha sido adivinada
            for (ClienteHandler cliente : clientesConectados) {
                cliente.enviarMensaje("¡La palabra ha sido adivinada!");
            }
            // Cerrar las conexiones con todos los clientes
            cerrarConexiones();
        }
    }

    // Método para manejar la conexión del cliente
    public void conectarCliente() {
        try {
            serverSocket = new ServerSocket(12345);
            System.out.println("Esperando cliente...");

            while (true) {
                // Aceptar una nueva conexión de cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado");

                // Crear un nuevo manejador de cliente y agregarlo a la lista
                ClienteHandler clienteHandler = new ClienteHandler(clientSocket);
                clientesConectados.add(clienteHandler);

                // Iniciar un nuevo hilo para el cliente
                Thread clienteThread = new Thread(clienteHandler);
                clienteThread.start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        conectarCliente();
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        // Iniciar el servidor en un hilo aparte
        Thread serverThread = new Thread(servidor);
        serverThread.start();
    }

    private class ClienteHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;

        public ClienteHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                // Crear streams para la comunicación
                this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.output = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // Leer las letras enviadas por el cliente
                String letra;
                while ((letra = input.readLine()) != null) {
                    System.out.println("Letra recibida de un cliente: " + letra);
                    verificarLetra(letra.charAt(0)); // Verificar la letra enviada por el cliente
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void enviarPalabraOculta(String palabra) {
            output.println(palabra);
        }

        public void enviarMensaje (String mensaje){
                if (output != null) {
                    output.println(mensaje);
                }

        }


        public void cerrarConexion() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void cerrarConexiones() {
        for (ClienteHandler cliente : clientesConectados) {
            cliente.enviarMensaje("¡El juego ha terminado!");
            cliente.cerrarConexion();
        }
        System.out.println("Todas las conexiones se han cerrado.");

    }
}
