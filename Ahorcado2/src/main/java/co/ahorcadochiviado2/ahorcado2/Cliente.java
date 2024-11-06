package co.ahorcadochiviado2.ahorcado2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.io.*;
import java.net.*;

public class Cliente extends Application {

    private TextField tfPalabra;
    private TextField tfLetra;
    private Button btnEnviar;
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Configuración de la ventana principal
        primaryStage.setTitle("Juego del Ahorcado");

        tfPalabra = new TextField();
        tfPalabra.setEditable(false);
        tfPalabra.setStyle("-fx-font-size: 20px; -fx-text-fill: #2e8b57;");

        tfLetra = new TextField();
        tfLetra.setMaxWidth(40);
        tfLetra.setStyle("-fx-font-size: 20px;");

        btnEnviar = new Button("Enviar Letra");
        btnEnviar.setStyle("-fx-font-size: 15px; -fx-base: #b6e7c9;");
        btnEnviar.setOnAction(e -> enviarLetra());

        VBox layout = new VBox(10);
        layout.setPadding(new javafx.geometry.Insets(20));
        layout.getChildren().addAll(new Label("Palabra: "), tfPalabra, new Label("Ingresa una letra: "), tfLetra, btnEnviar);

        Scene scene = new Scene(layout, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();

        conectar();
    }

    private void enviarLetra() {
        try {
            char letra = tfLetra.getText().charAt(0);
            out.println(letra);
            tfLetra.clear();  // Limpiar el campo de la letra
        } catch (Exception ex) {
            showAlert("Error", "Ingrese una letra válida.");
        }
    }

    private void conectar() {
        try {
            Socket socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Crear hilo para escuchar las actualizaciones de la palabra
            new Thread(() -> {
                try {
                    while (true) {
                        String palabra = in.readLine();
                        if (palabra != null) {
                            updatePalabra(palabra);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePalabra(String palabra) {
        // Se actualiza la palabra en la interfaz gráfica
        javafx.application.Platform.runLater(() -> tfPalabra.setText(palabra));
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
