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
    
    // Constructor
    public Servidor() {
        listaPalabras = new ArrayList<>();
        listaPalabras.add("CONEJO");
        listaPalabras.add("PERRO");
        listaPalabras.add("OBSERVABLE");
        listaPalabras.add("CLANMAMASITA");
        
        // Configuración de la interfaz gráfica
        frame = new JFrame("Servidor de Ahorcado");
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
        
        // Mostrar la palabra oculta en la interfaz
        lblPalabra.setText(palabraOculta);
        
        // Notificar al cliente
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                output.println(palabraOculta); // Enviar la palabra oculta al cliente
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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

        if (letraCorrecta == false){
            System.out.println("la letra ingresada no esta en la palabra");
        }
        palabraOculta = nuevaPalabra.toString();
        lblPalabra.setText(palabraOculta);

        // Enviar la actualización de la palabra oculta al cliente
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                output.println(palabraOculta); // Enviar la palabra oculta actualizada al cliente
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Si no hay asteriscos, la palabra está completa
        if (!palabraOculta.contains("*")) {
            JOptionPane.showMessageDialog(frame, "¡La palabra ha sido adivinada!");

clientSocket.close();
        }
    }

    // Método para manejar la conexión del cliente
    public void conectarCliente() {
        try {
            serverSocket = new ServerSocket(12345);
            System.out.println("Esperando cliente...");
            clientSocket = serverSocket.accept();
            System.out.println("Cliente conectado");

            // Crear streams para la comunicación
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);

            // Leer las letras enviadas por el cliente
            String letra;
            while ((letra = input.readLine()) != null) {
                System.out.println("Letra recibida: " + letra);
                verificarLetra(letra.charAt(0)); // Verificar la letra enviada por el cliente
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
}
