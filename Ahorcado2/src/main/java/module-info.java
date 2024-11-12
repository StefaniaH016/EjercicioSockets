module co.ahorcadochiviado.ahorcado {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens co.ahorcadochiviado2.ahorcado2 to javafx.fxml;
    exports co.ahorcadochiviado2.ahorcado2;
}