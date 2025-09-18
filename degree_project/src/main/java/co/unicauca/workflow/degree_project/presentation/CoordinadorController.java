package co.unicauca.workflow.degree_project.presentation;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class CoordinadorController implements Initializable{
    @FXML
    private Button btnProyectos;

    @FXML
    private Button btnSalir;
    
    @FXML
    private Label estado;

    private Button selectedButton = null; // botón actualmente seleccionado

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar eventos para cada botón
        btnProyectos.setOnAction(e -> selectButton(btnProyectos));
        btnSalir.setOnAction(e -> selectButton(btnSalir));
        
        // Establecer color según el texto del botón "estado"
        aplicarEstiloEstado();
    }

    private void aplicarEstiloEstado() {
        // limpiar estilos previos
        estado.getStyleClass().removeAll("estado-rojo", "estado-verde");

        String txt = (estado.getText() == null) ? "" : estado.getText().trim();

        if ("A evaluar".equalsIgnoreCase(txt)) {
            if (!estado.getStyleClass().contains("estado-rojo")) {
                estado.getStyleClass().add("estado-rojo");
            }
            // icono ojo abierto
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
            // icono ojo cerrado
            Image img = new Image(getClass().getResourceAsStream(
                    "/co/unicauca/workflow/degree_project/images/ojo_cerrado.png"));
            ImageView icon = new ImageView(img);
            icon.setFitWidth(24);
            icon.setFitHeight(22);
            estado.setGraphic(icon);


        } else {
            estado.setGraphic(null); // si no es ninguno, sin icono
        }
    }

    
    private void selectButton(Button button) {
        // Si ya hay un botón seleccionado, quitarle la clase CSS
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("selected");
        }
        // Agregar clase al nuevo botón seleccionado
        if (!button.getStyleClass().contains("selected")) {
            button.getStyleClass().add("selected");
        }
        selectedButton = button;
    }
    
}
