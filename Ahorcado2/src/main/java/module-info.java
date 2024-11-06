module co.ahorcadochiviado.ahorcado {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.ahorcadochiviado2.ahorcado2 to javafx.fxml;
    exports co.ahorcadochiviado2.ahorcado2;
}