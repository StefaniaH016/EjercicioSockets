package co.ahorcadochiviado2.ahorcado2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Observable;

public class Cliente extends Observable implements Runnable {
    private JFrame frame;
    private JLabel lblPalabra;
    private JButton btnEnviarLetra;
    private JTextField txtLetra;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String palabraOculta;

    // Constructor
    public Cliente() {
        // Configuración de la interfaz gráfica
        frame = new JFrame("Cliente - Juego de Ahorcado");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        lblPalabra = new JLabel("Palabra");
        frame.add(lblPalabra);

        txtLetra = new JTextField(2);
        frame.add(txtLetra);

        btnEnviarLetra = new JButton("Enviar Letra");
        btnEnviarLetra.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enviarLetra();
            }
        });
        frame.add(btnEnviarLetra);

        frame.setVisible(true);
    }

    // Método para conectar con el servidor
    public void conectarConServidor() {
        try {
            socket = new Socket("localhost", 12345);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            String mensaje;
            while ((mensaje = input.readLine()) != null) {
                // Si el mensaje es la palabra oculta, actualizarla
                if (mensaje.equals(palabraOculta)) {
                    actualizarPalabraOculta(palabraOculta);  // Extraer la palabra después del prefijo "Palabra: "
                } else if (mensaje.equals("¡La palabra ha sido adivinada!")) {
                    JOptionPane.showMessageDialog(frame, mensaje);
                    cerrarConexion();
                    break;
                } else if (mensaje.equals("¡El juego ha terminado!")) {
                    JOptionPane.showMessageDialog(frame, mensaje);
                    cerrarConexion();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para actualizar la palabra oculta y notificar a los observadores
    public void actualizarPalabraOculta(String nuevaPalabra) {
        this.palabraOculta = nuevaPalabra;
        setChanged();  // Marcar que ha habido un cambio
        notifyObservers();  // Notificar a los observadores
    }

    // Método para enviar la letra al servidor
    public void enviarLetra() {
        String letra = txtLetra.getText().toUpperCase();
        if (letra.length() == 1 && letra.matches("[A-Z]")) {
            output.println(letra);  // Enviar letra al servidor
            txtLetra.setText("");    // Limpiar el campo de texto
        } else {
            JOptionPane.showMessageDialog(frame, "Por favor, ingrese una letra válida.");
        }
    }

    // Método para actualizar la interfaz gráfica
    public void actualizarInterfaz() {
        lblPalabra.setText(palabraOculta);
    }

    public void cerrarConexion() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Conexión cerrada con el servidor.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        conectarConServidor();
    }

    public static void main(String[] args) {
        Cliente cliente = new Cliente();
        Cliente cliente2= new Cliente();
        
        // Registrar el cliente como observador de su propia interfaz gráfica
        cliente.addObserver((o, arg) -> {
            // Cuando la palabra oculta cambie, actualizar la interfaz
            cliente.actualizarInterfaz();

        });

        cliente2.addObserver((o, arg) -> {
            // Cuando la palabra oculta cambie, actualizar la interfaz
            cliente2.actualizarInterfaz();

        });

        // Iniciar el hilo para ejecutar la conexión y comunicación
        Thread clienteThread = new Thread(cliente);
        clienteThread.start();

        Thread clienteThread2 = new Thread(cliente2);
        clienteThread2.start();
    }
}
