package co.unicauca.workflow.degree_project.presentation;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;

public class Co_Observaciones_Controller implements Initializable{
    private CoordinadorController parent;

    @FXML
    private Label volver; // asegúrate de que en el FXML tenga fx:id="volver"
    
    @FXML
    private ChoiceBox<String> cbxValoracion;

    public void setParentController(CoordinadorController parent) {
        this.parent = parent;
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Acción al hacer clic en el Label volver
        volver.setOnMouseClicked(e -> {
            if (parent != null) {
                parent.loadUI("Coordinador_Proyectos");
            }
        });
        
        // Crear lista de elementos
        ObservableList<String> opciones = FXCollections.observableArrayList(
            "ACEPTADO",
            "RECHAZADO"
        );

        // Asignar lista a la combo
        cbxValoracion.setItems(opciones);
    }
    
}
