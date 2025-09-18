package co.unicauca.workflow.degree_project.presentation;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Co_Proyecto_Controller implements Initializable{
    private CoordinadorController parent;
    
    @FXML
    private Button estado;

    // Método para inyectar referencia al padre
    public void setParentController(CoordinadorController parent) {
        this.parent = parent;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        aplicarEstiloEstado();

        // Escuchar cambios de texto en el botón "estado"
        estado.textProperty().addListener((obs, oldText, newText) -> aplicarEstiloEstado());
        
        estado.setOnAction(e -> {
            if (parent != null) {
                parent.loadUI("Coordinador_Observaciones"); 
            }
        });
    }

    private void aplicarEstiloEstado() {
        estado.getStyleClass().removeAll("estado-rojo", "estado-verde");

        String txt = (estado.getText() == null) ? "" : estado.getText().trim();

        if ("A evaluar".equalsIgnoreCase(txt)) {
            if (!estado.getStyleClass().contains("estado-rojo")) {
                estado.getStyleClass().add("estado-rojo");
            }
            Image img = new Image(getClass().getResourceAsStream(
                    "/co/unicauca/workflow/degree_project/images/ojo_abierto.png"));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(24);
            icon.setFitHeight(22);
            estado.setGraphic(icon);

        } else if ("Evaluado".equalsIgnoreCase(txt)) {
            if (!estado.getStyleClass().contains("estado-verde")) {
                estado.getStyleClass().add("estado-verde");
            }
            Image img = new Image(getClass().getResourceAsStream(
                    "/co/unicauca/workflow/degree_project/images/ojo_cerrado.png"));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(24);
            icon.setFitHeight(22);
            estado.setGraphic(icon);

        } else {
            estado.setGraphic(null);
        }
    }

}
